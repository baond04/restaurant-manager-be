package com.restaurant.manager.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.response.OrderItemResponse;
import com.restaurant.manager.dto.response.OrderResponse;
import com.restaurant.manager.entity.DiningTable;
import com.restaurant.manager.entity.Item;
import com.restaurant.manager.entity.Order;
import com.restaurant.manager.entity.OrderItem;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.DiningTableRepository;
import com.restaurant.manager.repository.ItemRepository;
import com.restaurant.manager.repository.OrderItemRepository;
import com.restaurant.manager.repository.OrderRepository;
import com.restaurant.manager.repository.ServiceRequestRepository;
import com.restaurant.manager.security.CurrentUser;

/**
 * Phiên bàn và gọi món. Không có màn hình bếp: món gọi xong là ORDERED (nhân viên báo bếp bằng lời), trừ kho theo công thức ngay lúc gọi.
 * Khách tại bàn chỉ gọi món vào đúng đơn đang mở của bàn mình (orderId trong token), không tin orderId client gửi.
 */
@Service
public class OrderService {

	/** Quyền xem toàn bộ đơn: vận hành, thu ngân, báo cáo. */
	static final Set<String> VIEW = Set.of("order:view", "order:create", "table:view", "bill:view", "dashboard:view", "report:view", "customer:view",
			"request:handle");

	private final OrderRepository orders;
	private final OrderItemRepository lines;
	private final DiningTableRepository tables;
	private final ItemRepository items;
	private final ServiceRequestRepository requests;
	private final InventoryService inventory;

	public OrderService(OrderRepository orders, OrderItemRepository lines, DiningTableRepository tables, ItemRepository items,
			ServiceRequestRepository requests, InventoryService inventory) {
		this.orders = orders;
		this.lines = lines;
		this.tables = tables;
		this.items = items;
		this.requests = requests;
		this.inventory = inventory;
	}

	public record Line(Long itemId, Integer qty, String note, String source) {
	}

	/** Nhân viên thấy mọi đơn; khách có tài khoản thấy đơn của mình; khách tại bàn thấy đơn của bàn mình. */
	@Transactional(readOnly = true)
	public List<OrderResponse> list() {
		CurrentUser p = CurrentUser.get();
		return orders.findAll().stream().filter(o -> visible(p, o)).map(OrderResponse::of).toList();
	}

	@Transactional(readOnly = true)
	public List<OrderItemResponse> lines() {
		CurrentUser p = CurrentUser.get();
		if (p.isStaff() && p.canAny(VIEW)) {
			return lines.findAll().stream().map(OrderItemResponse::of).toList();
		}
		var mine = orders.findAll().stream().filter(o -> visible(p, o)).map(Order::getId).toList();
		return lines.findAll().stream().filter(l -> mine.contains(l.getOrderId())).map(OrderItemResponse::of).toList();
	}

	@Transactional
	public OrderResponse open(long tableId, int guestCount, Long reservationId, Long customerId) {
		CurrentUser p = CurrentUser.get();
		p.require("table:operate");
		DiningTable t = lockTable(tableId);
		if (!Boolean.TRUE.equals(t.getIsActive())) {
			throw ApiException.conflict("Bàn " + t.getCode() + " đang ngừng dùng");
		}
		if ("OCCUPIED".equals(t.getStatus())) {
			throw ApiException.conflict("Bàn " + t.getCode() + " đang có khách");
		}
		var o = new Order();
		o.setTableId(tableId);
		o.setReservationId(reservationId);
		o.setCustomerId(customerId);
		o.setOpenedBy(p.userId());
		o.setGuestCount(Math.max(1, guestCount));
		t.setStatus("OCCUPIED");
		requests.closeByTableAndType(tableId, "OPEN_TABLE"); // khách đã báo mở bàn thì yêu cầu đó xong
		return OrderResponse.of(orders.save(o));
	}

	@Transactional
	public OrderResponse move(long orderId, long toTableId) {
		CurrentUser.get().require("table:operate");
		Order o = openOrder(orderId);
		DiningTable to = lockTable(toTableId);
		if (!"AVAILABLE".equals(to.getStatus())) {
			throw ApiException.conflict("Bàn " + to.getCode() + " không trống");
		}
		lockTable(o.getTableId()).setStatus("CLEANING");
		o.setTableId(toTableId);
		to.setStatus("OCCUPIED");
		return OrderResponse.of(o);
	}

	/** Gọi món: nhân viên gọi hộ (có thể mở đơn mang đi), hoặc khách tại bàn gọi vào đơn của mình. */
	@Transactional
	public OrderResponse addItems(Long orderId, Long tableId, String type, List<Line> req, String source) {
		CurrentUser p = CurrentUser.get();
		if (req == null || req.isEmpty()) {
			throw ApiException.badRequest("Chưa chọn món");
		}
		Order o;
		if (p.isGuest()) {
			o = openOrder(p.orderId());
		} else {
			p.require("order:create");
			o = orderId != null ? openOrder(orderId) : tableId != null ? orders.findFirstByTableIdAndStatus(tableId, "OPEN").orElse(null) : null;
			if (o == null) {
				if (tableId != null) {
					throw ApiException.conflict("Bàn chưa mở, hãy mở bàn trước");
				}
				o = new Order(); // mang đi: không có bàn
				o.setType("TAKEAWAY".equals(type) ? "TAKEAWAY" : "DINE_IN");
				o.setOpenedBy(p.userId());
				o = orders.save(o);
			}
		}
		String fallback = source != null ? source : p.isStaff() ? "STAFF" : "MENU";
		for (Line l : req) {
			Item it = items.findById(l.itemId() == null ? -1 : l.itemId()).filter(i -> i.getDeletedAt() == null)
					.orElseThrow(() -> ApiException.notFound("Món không còn trong thực đơn"));
			if (!Boolean.TRUE.equals(it.getIsAvailable())) {
				throw ApiException.conflict(it.getName() + " đã hết");
			}
			int qty = l.qty() == null ? 1 : l.qty();
			if (qty < 1 || qty > 50) {
				throw ApiException.badRequest("Số lượng phải từ 1 đến 50");
			}
			var line = new OrderItem();
			line.setOrderId(o.getId());
			line.setItemId(it.getId());
			line.setQty(qty);
			line.setUnitPrice(it.getPrice()); // giá tại lúc gọi
			line.setNote(l.note());
			line.setSource(Arrays.asList("MENU", "CHATBOT", "STAFF").contains(l.source()) ? l.source() : fallback);
			inventory.consume(it, qty, lines.save(line));
		}
		return OrderResponse.of(o);
	}

	@Transactional
	public OrderResponse cancel(long orderId) {
		CurrentUser.get().require("order:create");
		Order o = openOrder(orderId);
		if (lines.existsByOrderIdAndStatusNot(orderId, "CANCELLED")) {
			throw ApiException.conflict("Đơn đã có món, không hủy được");
		}
		o.setStatus("CANCELLED");
		o.setClosedAt(Instant.now());
		if (o.getTableId() != null) {
			lockTable(o.getTableId()).setStatus("AVAILABLE");
		}
		return OrderResponse.of(o);
	}

	@Transactional
	public OrderItemResponse cancelLine(long lineId) {
		CurrentUser.get().require("order:cancel_item");
		OrderItem l = Repos.find(lines, lineId);
		openOrder(l.getOrderId());
		l.setStatus("CANCELLED");
		return OrderItemResponse.of(l);
	}

	/** Khách quét QR: bàn đang mở thì trả đơn, chưa mở thì 409 để giao diện hiện "Báo nhân viên mở bàn". */
	@Transactional(readOnly = true)
	public Order join(String qrToken) {
		DiningTable t = tableByQr(qrToken);
		return orders.findFirstByTableIdAndStatus(t.getId(), "OPEN")
				.orElseThrow(() -> ApiException.conflict("Bàn " + t.getCode() + " chưa mở. Vui lòng báo nhân viên."));
	}

	public DiningTable tableByQr(String qrToken) {
		return tables.findByQrTokenAndIsActiveTrue(qrToken == null ? "" : qrToken).orElseThrow(() -> ApiException.notFound("Mã bàn không còn hiệu lực"));
	}

	/** Đơn đang mở, đã khóa dòng: hai người thao tác cùng lúc thì một người nhận 409. */
	Order openOrder(long orderId) {
		Order o = orders.lockById(orderId).orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn"));
		if (!"OPEN".equals(o.getStatus())) {
			throw ApiException.conflict("Đơn đã đóng");
		}
		return o;
	}

	DiningTable lockTable(long id) {
		return tables.lockById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy bàn"));
	}

	static boolean visible(CurrentUser p, Order o) {
		if (p.isStaff() && p.canAny(VIEW)) {
			return true;
		}
		return p.isCustomer() ? p.userId().equals(o.getCustomerId()) : o.getId().equals(p.orderId());
	}

}

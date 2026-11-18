package com.restaurant.manager.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.response.BillResponse;
import com.restaurant.manager.entity.Area;
import com.restaurant.manager.entity.Bill;
import com.restaurant.manager.entity.Order;
import com.restaurant.manager.entity.User;
import com.restaurant.manager.entity.Voucher;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.AreaRepository;
import com.restaurant.manager.repository.BillRepository;
import com.restaurant.manager.repository.DiningTableRepository;
import com.restaurant.manager.repository.OrderItemRepository;
import com.restaurant.manager.repository.OrderRepository;
import com.restaurant.manager.repository.ServiceRequestRepository;
import com.restaurant.manager.repository.UserRepository;
import com.restaurant.manager.repository.VoucherRepository;
import com.restaurant.manager.security.CurrentUser;

/** Thanh toán một lần sau khi ăn. Số tiền luôn tính lại ở máy chủ bằng BillCalculator, không tin số client gửi. */
@Service
public class BillingService {

	private final BillRepository bills;
	private final OrderRepository orders;
	private final OrderItemRepository lines;
	private final DiningTableRepository tables;
	private final AreaRepository areas;
	private final VoucherRepository vouchers;
	private final UserRepository users;
	private final ServiceRequestRepository requests;
	private final OrderService orderService;

	public BillingService(BillRepository bills, OrderRepository orders, OrderItemRepository lines, DiningTableRepository tables, AreaRepository areas,
			VoucherRepository vouchers, UserRepository users, ServiceRequestRepository requests, OrderService orderService) {
		this.bills = bills;
		this.orders = orders;
		this.lines = lines;
		this.tables = tables;
		this.areas = areas;
		this.vouchers = vouchers;
		this.users = users;
		this.requests = requests;
		this.orderService = orderService;
	}

	/** Nhân viên thấy mọi hóa đơn; khách thấy hóa đơn của đơn mình. */
	@Transactional(readOnly = true)
	public List<BillResponse> list() {
		CurrentUser p = CurrentUser.get();
		boolean staff = p.isStaff() && p.canAny(Set.of("bill:view", "report:view", "dashboard:view", "customer:view"));
		return bills.findAll().stream().filter(b -> staff || orders.findById(b.getOrderId()).filter(o -> OrderService.visible(p, o)).isPresent())
				.map(BillResponse::of).toList();
	}

	/** Tạm tính, không lưu; khách tại bàn xem được tạm tính của bàn mình. */
	@Transactional(readOnly = true)
	public BillCalculator.Result preview(long orderId, Long voucherId, int pointsUsed) {
		Order o = Repos.find(orders, orderId);
		if (!OrderService.visible(CurrentUser.get(), o)) {
			throw ApiException.forbidden();
		}
		return compute(o, voucherId == null ? null : Repos.find(vouchers, voucherId), pointsUsed);
	}

	@Transactional
	public BillResponse pay(long orderId, Long voucherId, int pointsUsed, String method) {
		CurrentUser p = CurrentUser.get();
		p.require("bill:pay");
		if ((voucherId != null || pointsUsed > 0) && !p.can("bill:discount")) {
			throw ApiException.forbidden();
		}
		ApiException.requireOneOf(method, "Phương thức thanh toán không hợp lệ", "CASH", "BANK_TRANSFER", "CARD");
		Order o = orderService.openOrder(orderId);
		Voucher v = voucherId == null ? null : vouchers.lockById(voucherId).orElseThrow(() -> ApiException.notFound("Không tìm thấy voucher"));
		User customer = o.getCustomerId() == null ? null : users.lockById(o.getCustomerId()).orElseThrow();
		if (pointsUsed < 0 || pointsUsed > (customer == null ? 0 : customer.getPoints())) {
			throw ApiException.badRequest("Khách không đủ điểm");
		}
		var r = compute(o, v, pointsUsed);
		if (r.subtotal() == 0) {
			throw ApiException.badRequest("Đơn chưa có món, hãy hủy đơn thay vì thanh toán");
		}
		if (v != null && r.discount() == 0) {
			throw ApiException.badRequest("Voucher không áp dụng được cho hóa đơn này");
		}
		var b = new Bill();
		b.setOrderId(orderId);
		b.setVoucherId(v == null ? null : v.getId());
		b.setSubtotal(r.subtotal());
		b.setServiceFee(r.serviceFee());
		b.setMinSpendTopUp(r.minSpendTopUp());
		b.setDiscount(r.discount());
		b.setPointsAmount(r.pointsAmount());
		b.setTax(r.tax());
		b.setTotal(r.total());
		b.setPointsUsed(pointsUsed);
		b.setPointsEarned(customer == null ? 0 : r.pointsEarned());
		b.setMethod(method);
		b.setCashierId(p.userId());
		if (v != null) {
			v.setUsedCount(v.getUsedCount() + 1);
		}
		if (customer != null) {
			customer.setPoints(customer.getPoints() - pointsUsed + r.pointsEarned());
		}
		o.setStatus("COMPLETED"); // đơn đóng: token khách tại bàn hết hiệu lực từ request kế tiếp
		o.setClosedAt(Instant.now());
		if (o.getTableId() != null) {
			orderService.lockTable(o.getTableId()).setStatus("CLEANING");
		}
		requests.closeByOrder(orderId);
		return BillResponse.of(bills.save(b));
	}

	@Transactional
	public BillResponse voidBill(long billId) {
		CurrentUser.get().require("bill:void"); // chỉ quản lý: chống thất thoát
		Bill b = bills.lockById(billId).orElseThrow(() -> ApiException.notFound("Không tìm thấy hóa đơn"));
		if (!"PAID".equals(b.getStatus())) {
			throw ApiException.conflict("Chỉ hủy được hóa đơn đã thanh toán");
		}
		b.setStatus("VOID");
		return BillResponse.of(b);
	}

	private BillCalculator.Result compute(Order o, Voucher v, int pointsUsed) {
		Area area = o.getTableId() == null ? null : tables.findById(o.getTableId()).flatMap(t -> areas.findById(t.getAreaId())).orElse(null);
		BigDecimal fee = area == null ? BigDecimal.ZERO : area.getServiceFeePercent();
		long minSpend = area == null ? 0 : area.getMinSpend();
		return BillCalculator.calc(lines.findByOrderId(o.getId()), fee, minSpend, v, pointsUsed, Instant.now());
	}

}

package com.restaurant.manager.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.ReservationRequest;
import com.restaurant.manager.dto.response.ReservationResponse;
import com.restaurant.manager.entity.Area;
import com.restaurant.manager.entity.DiningTable;
import com.restaurant.manager.entity.Order;
import com.restaurant.manager.entity.Reservation;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.AreaRepository;
import com.restaurant.manager.repository.OrderRepository;
import com.restaurant.manager.repository.ReservationRepository;
import com.restaurant.manager.repository.UserRepository;
import com.restaurant.manager.security.CurrentUser;

/**
 * Đặt bàn tự xác nhận (không có người duyệt), nên máy chủ tự kiểm tra: khóa bàn theo thứ tự id rồi kiểm trùng ±2 giờ, đủ chỗ,
 * phòng VIP từ 10 khách, đặt trước ít nhất 30 phút và tối đa 60 ngày, giới hạn số lượt đang hiệu lực.
 */
@Service
public class ReservationService {

	static final Duration SLOT = Duration.ofHours(2);

	private final ReservationRepository reservations;
	private final AreaRepository areas;
	private final OrderRepository orders;
	private final UserRepository users;
	private final OrderService orderService;

	public ReservationService(ReservationRepository reservations, AreaRepository areas, OrderRepository orders, UserRepository users,
			OrderService orderService) {
		this.reservations = reservations;
		this.areas = areas;
		this.orders = orders;
		this.users = users;
		this.orderService = orderService;
	}

	/** Nhân viên thấy mọi lượt; khách thấy lượt của mình. */
	@Transactional(readOnly = true)
	public List<ReservationResponse> list() {
		CurrentUser p = CurrentUser.get();
		if (p.isStaff() && p.can("reservation:view")) {
			return reservations.findAll().stream().map(ReservationResponse::of).toList();
		}
		if (!p.isCustomer()) {
			throw ApiException.forbidden();
		}
		return reservations.findAll().stream().filter(r -> p.userId().equals(r.getCustomerId())).map(ReservationResponse::of).toList();
	}

	@Transactional
	public ReservationResponse create(ReservationRequest req) {
		CurrentUser p = CurrentUser.get();
		boolean staff = p.can("reservation:manage");
		validate(req, p, staff);
		List<Long> ids = req.tableIds().stream().distinct().sorted().toList();
		Long areaId = lockFreeTables(ids, req, -1L);
		if (!staff) {
			limitActive(p, req.guestPhone());
		}
		var r = new Reservation();
		r.setCustomerId(p.isCustomer() ? p.userId() : null);
		r.setGuestName(p.isCustomer() && req.guestName() == null ? Repos.find(users, p.userId()).getFullName() : req.guestName().trim());
		r.setGuestPhone(req.guestPhone());
		r.setTime(req.time());
		r.setGuestCount(req.guestCount());
		r.setAreaId(areaId);
		r.setNote(req.note());
		r.setTableIds(new ArrayList<>(ids));
		return ReservationResponse.of(reservations.save(r));
	}

	/** Nhân viên đổi giờ, số khách, bàn, thông tin liên hệ của lượt còn hiệu lực; kiểm tra lại như khi tạo, bỏ qua chính lượt này. */
	@Transactional
	public ReservationResponse update(long id, ReservationRequest req) {
		CurrentUser p = CurrentUser.get();
		p.require("reservation:manage");
		Reservation r = reservations.lockById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy lượt đặt"));
		if (!"CONFIRMED".equals(r.getStatus())) {
			throw ApiException.conflict("Chỉ sửa được lượt đặt đang chờ khách đến");
		}
		validate(req, p, true);
		List<Long> ids = req.tableIds().stream().distinct().sorted().toList();
		r.setAreaId(lockFreeTables(ids, req, r.getId()));
		r.setGuestName(req.guestName().trim());
		r.setGuestPhone(req.guestPhone());
		r.setTime(req.time());
		r.setGuestCount(req.guestCount());
		r.setNote(req.note());
		r.setTableIds(new ArrayList<>(ids));
		return ReservationResponse.of(r);
	}

	/** Khóa bàn theo thứ tự id (hai người đặt cùng lúc không khóa chéo), kiểm trùng ±2 giờ, phòng VIP, đủ chỗ. Trả khu vực. */
	private Long lockFreeTables(List<Long> ids, ReservationRequest req, Long exceptReservationId) {
		int seats = 0;
		Long areaId = null;
		for (Long id : ids) {
			DiningTable t = orderService.lockTable(id);
			if (!Boolean.TRUE.equals(t.getIsActive())) {
				throw ApiException.badRequest("Bàn " + t.getCode() + " không còn sử dụng");
			}
			if (reservations.tableBusy(t.getId(), req.time().minus(SLOT), req.time().plus(SLOT), exceptReservationId)) {
				throw ApiException.conflict("Bàn " + t.getCode() + " vừa có người đặt");
			}
			Area a = Repos.find(areas, t.getAreaId());
			if ("VIP_ROOM".equals(a.getType()) && req.guestCount() < 10) {
				throw ApiException.badRequest("Phòng VIP nhận từ 10 khách");
			}
			seats += t.getSeats();
			areaId = t.getAreaId();
		}
		if (seats < req.guestCount()) {
			throw ApiException.badRequest("Các bàn đã chọn chỉ có " + seats + " chỗ");
		}
		return areaId;
	}

	/** Nhân viên nhận bàn, đánh dấu không đến, hủy; khách chỉ hủy lượt của mình. */
	@Transactional
	public ReservationResponse setStatus(long id, String status) {
		CurrentUser p = CurrentUser.get();
		Reservation r = reservations.lockById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy lượt đặt"));
		boolean owner = p.isCustomer() && p.userId().equals(r.getCustomerId());
		if (!(p.can("reservation:manage") || owner && "CANCELLED".equals(status))) {
			throw ApiException.forbidden();
		}
		if (!"CONFIRMED".equals(r.getStatus()) && !"SEATED".equals(r.getStatus())) {
			throw ApiException.conflict("Lượt đặt đã kết thúc");
		}
		ApiException.requireOneOf(status, "Trạng thái không hợp lệ: " + status, "SEATED", "CANCELLED", "NO_SHOW", "COMPLETED");
		if ("SEATED".equals(status)) {
			seat(r, p);
		}
		r.setStatus(status);
		return ReservationResponse.of(r);
	}

	/** Bàn nào đã có lượt đặt quanh khoảng giờ; không kèm thông tin khách nên trang đặt bàn công khai dùng được. */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> busySlots(Instant from, Instant to) {
		return reservations.findByStatusInAndTimeBetween(List.of("CONFIRMED", "SEATED"), from.minus(SLOT), to.plus(SLOT)).stream()
				.flatMap(r -> r.getTableIds().stream().map(t -> Map.<String, Object>of("tableId", t, "time", r.getTime()))).toList();
	}

	private void seat(Reservation r, CurrentUser p) {
		if (r.getTableIds().isEmpty()) {
			throw ApiException.badRequest("Chưa gán bàn cho lượt đặt này");
		}
		var tables = r.getTableIds().stream().sorted().map(orderService::lockTable).toList();
		tables.stream().filter(t -> "OCCUPIED".equals(t.getStatus())).findFirst().ifPresent(t -> {
			throw ApiException.conflict("Bàn " + t.getCode() + " đang có khách");
		});
		tables.forEach(t -> t.setStatus("OCCUPIED"));
		var o = new Order(); // một đơn cho cả lượt đặt, gắn với bàn có id nhỏ nhất
		o.setTableId(tables.stream().min(Comparator.comparing(DiningTable::getId)).orElseThrow().getId());
		o.setReservationId(r.getId());
		o.setCustomerId(r.getCustomerId());
		o.setOpenedBy(p.userId());
		o.setGuestCount(r.getGuestCount());
		orders.save(o);
	}

	private static void validate(ReservationRequest req, CurrentUser p, boolean staff) {
		if (req.time() == null || req.guestCount() == null || req.guestCount() < 1 || req.tableIds() == null || req.tableIds().isEmpty()) {
			throw ApiException.badRequest("Thiếu giờ đến, số khách hoặc bàn");
		}
		if (req.guestPhone() == null || !req.guestPhone().matches("0\\d{9}")) {
			throw ApiException.badRequest("Số điện thoại phải là 10 số, bắt đầu bằng 0");
		}
		if (!p.isCustomer() && (req.guestName() == null || req.guestName().isBlank())) {
			throw ApiException.badRequest("Thiếu họ tên");
		}
		Instant now = Instant.now();
		if (!staff && req.time().isBefore(now.plus(Duration.ofMinutes(30)))) {
			throw ApiException.badRequest("Cần đặt trước ít nhất 30 phút");
		}
		if (req.time().isAfter(now.plus(Duration.ofDays(60)))) {
			throw ApiException.badRequest("Chỉ đặt trước tối đa 60 ngày");
		}
	}

	/** Tài khoản tối đa 2 lượt sắp tới; khách không đăng nhập tối đa 1 lượt theo số điện thoại. */
	private void limitActive(CurrentUser p, String phone) {
		long n = p.isCustomer() ? reservations.countByCustomerIdAndStatusAndTimeAfter(p.userId(), "CONFIRMED", Instant.now())
				: reservations.countByGuestPhoneAndStatusAndTimeAfter(phone, "CONFIRMED", Instant.now());
		if (n >= (p.isCustomer() ? 2 : 1)) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
					p.isCustomer() ? "Mỗi tài khoản giữ tối đa 2 lượt đặt sắp tới" : "Số điện thoại này đã có lượt đặt sắp tới");
		}
	}

}

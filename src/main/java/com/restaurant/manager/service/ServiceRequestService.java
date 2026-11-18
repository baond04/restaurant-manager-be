package com.restaurant.manager.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.response.ServiceRequestResponse;
import com.restaurant.manager.entity.DiningTable;
import com.restaurant.manager.entity.Order;
import com.restaurant.manager.entity.ServiceRequest;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.OrderRepository;
import com.restaurant.manager.repository.ServiceRequestRepository;
import com.restaurant.manager.security.CurrentUser;

/** Yêu cầu của khách tại bàn (gọi nhân viên, tính tiền, thêm nước, báo mở bàn); nhân viên nhận rồi đánh dấu xong. */
@Service
public class ServiceRequestService {

	private final ServiceRequestRepository requests;
	private final OrderRepository orders;
	private final OrderService orderService;

	public ServiceRequestService(ServiceRequestRepository requests, OrderRepository orders, OrderService orderService) {
		this.requests = requests;
		this.orders = orders;
		this.orderService = orderService;
	}

	@Transactional(readOnly = true)
	public List<ServiceRequestResponse> list() {
		CurrentUser p = CurrentUser.get();
		boolean staff = p.isStaff() && p.canAny(Set.of("request:handle", "table:view", "report:view"));
		if (!staff && !p.isGuest()) {
			throw ApiException.forbidden();
		}
		return requests.findAll().stream().filter(s -> staff || p.orderId().equals(s.getOrderId())).map(ServiceRequestResponse::of).toList();
	}

	/** Bàn xác định bằng mã QR; mỗi bàn chỉ một yêu cầu cùng loại đang mở để bấm liên tục không làm đầy danh sách. */
	@Transactional
	public ServiceRequestResponse create(String qrToken, String type) {
		DiningTable t = orderService.tableByQr(qrToken);
		ApiException.requireOneOf(type, "Loại yêu cầu không hợp lệ", "CALL_STAFF", "REQUEST_BILL", "EXTRA_WATER", "EXTRA_UTENSIL", "OPEN_TABLE", "OTHER");
		if (requests.existsByTableIdAndTypeAndStatusNot(t.getId(), type, "DONE")) {
			throw ApiException.conflict("Yêu cầu này đã gửi, nhân viên sẽ đến ngay");
		}
		var s = new ServiceRequest();
		s.setTableId(t.getId());
		s.setOrderId(orders.findFirstByTableIdAndStatus(t.getId(), "OPEN").map(Order::getId).orElse(null));
		s.setType(type);
		return ServiceRequestResponse.of(requests.save(s));
	}

	@Transactional
	public ServiceRequestResponse handle(long id, boolean done) {
		CurrentUser p = CurrentUser.get();
		p.require("request:handle");
		ServiceRequest s = Repos.find(requests, id);
		if (done) {
			s.setStatus("DONE");
			s.setDoneAt(Instant.now());
		} else {
			s.setStatus("ACKNOWLEDGED");
			s.setAckAt(Instant.now());
		}
		if (s.getHandledBy() == null) {
			s.setHandledBy(p.userId());
		}
		return ServiceRequestResponse.of(s);
	}

}

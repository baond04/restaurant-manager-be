package com.restaurant.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.response.OrderItemResponse;
import com.restaurant.manager.dto.response.OrderResponse;
import com.restaurant.manager.entity.DiningTable;
import com.restaurant.manager.entity.Order;
import com.restaurant.manager.security.JwtService;
import com.restaurant.manager.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Phiên bàn và gọi món")
@RestController
@RequestMapping("/api/v1")
public class OrderController {

	private final OrderService orders;
	private final JwtService jwt;

	public OrderController(OrderService orders, JwtService jwt) {
		this.orders = orders;
		this.jwt = jwt;
	}

	public record OpenTableRequest(Integer guestCount, Long reservationId, Long customerId) {
	}

	public record AddItemsRequest(Long orderId, Long tableId, String type, List<OrderService.Line> lines, String source) {
	}

	@GetMapping("/orders")
	public List<OrderResponse> orders() {
		return orders.list();
	}

	@GetMapping("/order-items")
	public List<OrderItemResponse> orderItems() {
		return orders.lines();
	}

	@Operation(summary = "Mở bàn: tạo đơn đang mở (phiên bàn)")
	@PostMapping("/tables/{id}/open")
	public OrderResponse open(@PathVariable long id, @RequestBody OpenTableRequest req) {
		return orders.open(id, req.guestCount() == null ? 1 : req.guestCount(), req.reservationId(), req.customerId());
	}

	@PostMapping("/orders/{id}/move")
	public OrderResponse move(@PathVariable long id, @RequestBody Map<String, Long> body) {
		return orders.move(id, body.get("toTableId"));
	}

	@Operation(summary = "Gọi món: nhân viên gọi hộ, hoặc khách tại bàn gọi vào đơn của mình")
	@PostMapping("/orders/items")
	public OrderResponse addItems(@RequestBody AddItemsRequest req) {
		return orders.addItems(req.orderId(), req.tableId(), req.type(), req.lines(), req.source());
	}

	@PostMapping("/orders/{id}/cancel")
	public OrderResponse cancel(@PathVariable long id) {
		return orders.cancel(id);
	}

	@PostMapping("/order-items/{id}/cancel")
	public OrderItemResponse cancelLine(@PathVariable long id) {
		return orders.cancelLine(id);
	}

	@Operation(summary = "Khách quét QR: tên bàn và khu vực, kể cả khi bàn chưa mở (không trả mã QR)")
	@GetMapping("/table-sessions/table")
	public Map<String, Object> table(@RequestParam String qrToken) {
		DiningTable t = orders.tableByQr(qrToken);
		return Map.of("id", t.getId(), "code", t.getCode(), "areaId", t.getAreaId(), "seats", t.getSeats());
	}

	@Operation(summary = "Khách quét QR: trả token khách tại bàn nếu bàn đang mở")
	@PostMapping("/table-sessions/join")
	public Map<String, Object> join(@RequestBody Map<String, String> body) {
		Order o = orders.join(body.get("qrToken"));
		return Map.of("token", jwt.guestToken(o.getId(), o.getTableId()), "orderId", o.getId(), "tableId", o.getTableId());
	}

}

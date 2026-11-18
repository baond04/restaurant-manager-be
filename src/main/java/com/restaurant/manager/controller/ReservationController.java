package com.restaurant.manager.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.ReservationRequest;
import com.restaurant.manager.dto.response.ReservationResponse;
import com.restaurant.manager.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Đặt bàn")
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

	private final ReservationService reservations;

	public ReservationController(ReservationService reservations) {
		this.reservations = reservations;
	}

	@GetMapping
	public List<ReservationResponse> list() {
		return reservations.list();
	}

	@Operation(summary = "Đặt bàn (cả khách chưa đăng nhập), tự xác nhận nếu bàn trống trong ±2 giờ")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReservationResponse create(@Valid @RequestBody ReservationRequest req) {
		return reservations.create(req);
	}

	@Operation(summary = "Nhân viên sửa lượt đặt đang chờ (giờ, số khách, bàn, liên hệ)")
	@PatchMapping("/{id}")
	public ReservationResponse update(@PathVariable long id, @Valid @RequestBody ReservationRequest req) {
		return reservations.update(id, req);
	}

	@Operation(summary = "Nhận bàn (SEATED), không đến (NO_SHOW), hủy (CANCELLED), hoàn thành (COMPLETED)")
	@PostMapping("/{id}/status")
	public ReservationResponse status(@PathVariable long id, @RequestBody Map<String, String> body) {
		return reservations.setStatus(id, body.get("status"));
	}

	@Operation(summary = "Các bàn đã có lượt đặt quanh khoảng giờ (không kèm thông tin khách)")
	@GetMapping("/availability")
	public List<Map<String, Object>> availability(@RequestParam Instant from, @RequestParam Instant to) {
		return reservations.busySlots(from, to);
	}

}

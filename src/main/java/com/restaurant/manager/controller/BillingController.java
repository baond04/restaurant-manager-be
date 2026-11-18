package com.restaurant.manager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.response.BillResponse;
import com.restaurant.manager.service.BillCalculator;
import com.restaurant.manager.service.BillingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Thanh toán")
@RestController
@RequestMapping("/api/v1")
public class BillingController {

	private final BillingService billing;

	public BillingController(BillingService billing) {
		this.billing = billing;
	}

	public record PayRequest(Long voucherId, Integer pointsUsed, String method) {
	}

	@GetMapping("/bills")
	public List<BillResponse> bills() {
		return billing.list();
	}

	@Operation(summary = "Tạm tính hóa đơn (không lưu)")
	@GetMapping("/orders/{id}/bill-preview")
	public BillCalculator.Result preview(@PathVariable long id, @RequestParam(required = false) Long voucherId,
			@RequestParam(defaultValue = "0") int pointsUsed) {
		return billing.preview(id, voucherId, pointsUsed);
	}

	@Operation(summary = "Thu tiền: tính lại hóa đơn ở máy chủ, đóng đơn, cộng trừ điểm")
	@PostMapping("/orders/{id}/pay")
	public BillResponse pay(@PathVariable long id, @RequestBody PayRequest req) {
		return billing.pay(id, req.voucherId(), req.pointsUsed() == null ? 0 : req.pointsUsed(), req.method());
	}

	@Operation(summary = "Hủy hóa đơn đã thanh toán (chỉ quản lý)")
	@PostMapping("/bills/{id}/void")
	public BillResponse voidBill(@PathVariable long id) {
		return billing.voidBill(id);
	}

}

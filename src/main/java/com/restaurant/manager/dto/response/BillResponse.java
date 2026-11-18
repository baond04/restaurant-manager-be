package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Bill;

/** Dữ liệu Bill trả cho client, kèm bốn trường kiểm vết. */
public record BillResponse(
		Long id,
		Long orderId,
		Long voucherId,
		Long subtotal,
		Long serviceFee,
		Long minSpendTopUp,
		Long discount,
		Long pointsAmount,
		Long tax,
		Long total,
		Integer pointsUsed,
		Integer pointsEarned,
		String method,
		String status,
		Long cashierId,
		Instant paidAt,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static BillResponse of(Bill e) {
		return new BillResponse(e.getId(),
				e.getOrderId(),
				e.getVoucherId(),
				e.getSubtotal(),
				e.getServiceFee(),
				e.getMinSpendTopUp(),
				e.getDiscount(),
				e.getPointsAmount(),
				e.getTax(),
				e.getTotal(),
				e.getPointsUsed(),
				e.getPointsEarned(),
				e.getMethod(),
				e.getStatus(),
				e.getCashierId(),
				e.getPaidAt(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

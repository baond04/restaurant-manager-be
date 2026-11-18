package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.OrderItem;

/** Dữ liệu OrderItem trả cho client, kèm bốn trường kiểm vết. */
public record OrderItemResponse(
		Long id,
		Long orderId,
		Long itemId,
		Integer qty,
		Long unitPrice,
		String note,
		String status,
		String source,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static OrderItemResponse of(OrderItem e) {
		return new OrderItemResponse(e.getId(),
				e.getOrderId(),
				e.getItemId(),
				e.getQty(),
				e.getUnitPrice(),
				e.getNote(),
				e.getStatus(),
				e.getSource(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

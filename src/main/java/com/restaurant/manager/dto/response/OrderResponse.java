package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Order;

/** Dữ liệu Order trả cho client, kèm bốn trường kiểm vết. */
public record OrderResponse(
		Long id,
		Long tableId,
		Long reservationId,
		Long customerId,
		Long openedBy,
		String type,
		Integer guestCount,
		String status,
		String note,
		Instant openedAt,
		Instant closedAt,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static OrderResponse of(Order e) {
		return new OrderResponse(e.getId(),
				e.getTableId(),
				e.getReservationId(),
				e.getCustomerId(),
				e.getOpenedBy(),
				e.getType(),
				e.getGuestCount(),
				e.getStatus(),
				e.getNote(),
				e.getOpenedAt(),
				e.getClosedAt(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

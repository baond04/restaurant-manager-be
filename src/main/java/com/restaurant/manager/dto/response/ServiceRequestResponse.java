package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.ServiceRequest;

/** Dữ liệu ServiceRequest trả cho client, kèm bốn trường kiểm vết. */
public record ServiceRequestResponse(
		Long id,
		Long tableId,
		Long orderId,
		String type,
		String status,
		Long handledBy,
		Instant ackAt,
		Instant doneAt,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ServiceRequestResponse of(ServiceRequest e) {
		return new ServiceRequestResponse(e.getId(),
				e.getTableId(),
				e.getOrderId(),
				e.getType(),
				e.getStatus(),
				e.getHandledBy(),
				e.getAckAt(),
				e.getDoneAt(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

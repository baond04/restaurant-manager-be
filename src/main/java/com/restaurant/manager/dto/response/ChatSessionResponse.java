package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.ChatSession;

/** Dữ liệu ChatSession trả cho client, kèm bốn trường kiểm vết. */
public record ChatSessionResponse(
		String id,
		Long customerId,
		Long orderId,
		String title,
		Integer turns,
		Integer feedback,
		Boolean addedToCart,
		Integer latencyMs,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ChatSessionResponse of(ChatSession e) {
		return new ChatSessionResponse(e.getId(),
				e.getCustomerId(),
				e.getOrderId(),
				e.getTitle(),
				e.getTurns(),
				e.getFeedback(),
				e.getAddedToCart(),
				e.getLatencyMs(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.ChatMessage;

/** Dữ liệu ChatMessage trả cho client, kèm bốn trường kiểm vết. */
public record ChatMessageResponse(
		Long id,
		String sessionId,
		String role,
		String content,
		String itemIds,
		String intent,
		Integer latencyMs,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ChatMessageResponse of(ChatMessage e) {
		return new ChatMessageResponse(e.getId(),
				e.getSessionId(),
				e.getRole(),
				e.getContent(),
				e.getItemIds(),
				e.getIntent(),
				e.getLatencyMs(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

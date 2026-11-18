package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Tag;

/** Dữ liệu Tag trả cho client, kèm bốn trường kiểm vết. */
public record TagResponse(
		Long id,
		String name,
		String type,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static TagResponse of(Tag e) {
		return new TagResponse(e.getId(),
				e.getName(),
				e.getType(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

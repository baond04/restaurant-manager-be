package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Category;

/** Dữ liệu Category trả cho client, kèm bốn trường kiểm vết. */
public record CategoryResponse(
		Long id,
		String name,
		Integer sortOrder,
		Boolean isActive,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static CategoryResponse of(Category e) {
		return new CategoryResponse(e.getId(),
				e.getName(),
				e.getSortOrder(),
				e.getIsActive(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

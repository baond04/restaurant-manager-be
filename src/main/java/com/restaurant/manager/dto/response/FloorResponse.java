package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Floor;

/** Dữ liệu Floor trả cho client, kèm bốn trường kiểm vết. */
public record FloorResponse(
		Long id,
		String name,
		Integer level,
		Boolean isActive,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static FloorResponse of(Floor e) {
		return new FloorResponse(e.getId(),
				e.getName(),
				e.getLevel(),
				e.getIsActive(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

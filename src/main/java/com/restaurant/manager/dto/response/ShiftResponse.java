package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Shift;

/** Dữ liệu Shift trả cho client, kèm bốn trường kiểm vết. */
public record ShiftResponse(
		Long id,
		String name,
		String start,
		String end,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ShiftResponse of(Shift e) {
		return new ShiftResponse(e.getId(),
				e.getName(),
				e.getStart(),
				e.getEnd(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

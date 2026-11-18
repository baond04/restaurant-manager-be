package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Review;

/** Dữ liệu Review trả cho client, kèm bốn trường kiểm vết. */
public record ReviewResponse(
		Long id,
		Long customerId,
		Long itemId,
		Integer rating,
		String comment,
		Boolean hidden,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ReviewResponse of(Review e) {
		return new ReviewResponse(e.getId(),
				e.getCustomerId(),
				e.getItemId(),
				e.getRating(),
				e.getComment(),
				e.getHidden(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

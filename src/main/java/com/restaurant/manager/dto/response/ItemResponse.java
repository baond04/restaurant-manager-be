package com.restaurant.manager.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.restaurant.manager.entity.Item;
import com.restaurant.manager.entity.RecipeLine;

/** Dữ liệu Item trả cho client, kèm bốn trường kiểm vết. */
public record ItemResponse(
		Long id,
		Long categoryId,
		String name,
		String description,
		Long price,
		String image,
		Boolean isAvailable,
		Integer spicyLevel,
		Integer prepMinutes,
		Instant deletedAt,
		Set<Long> tagIds,
		List<RecipeLine> recipe,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ItemResponse of(Item e) {
		return new ItemResponse(e.getId(),
				e.getCategoryId(),
				e.getName(),
				e.getDescription(),
				e.getPrice(),
				e.getImage(),
				e.getIsAvailable(),
				e.getSpicyLevel(),
				e.getPrepMinutes(),
				e.getDeletedAt(),
				e.getTagIds(),
				e.getRecipe(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

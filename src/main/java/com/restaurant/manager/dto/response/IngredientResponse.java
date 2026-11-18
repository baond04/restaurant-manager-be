package com.restaurant.manager.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.restaurant.manager.entity.Ingredient;

/** Dữ liệu Ingredient trả cho client, kèm bốn trường kiểm vết. */
public record IngredientResponse(
		Long id,
		String name,
		String unit,
		BigDecimal stock,
		BigDecimal minStock,
		Long cost,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static IngredientResponse of(Ingredient e) {
		return new IngredientResponse(e.getId(),
				e.getName(),
				e.getUnit(),
				e.getStock(),
				e.getMinStock(),
				e.getCost(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

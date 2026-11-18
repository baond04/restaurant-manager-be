package com.restaurant.manager.dto.request;

import java.math.BigDecimal;

import com.restaurant.manager.entity.Ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Ingredient. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record IngredientRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 80) String name,
		@NotBlank(groups = OnCreate.class) @Size(max = 20) String unit,
		@PositiveOrZero BigDecimal minStock,
		@PositiveOrZero Long cost) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Ingredient e) {
		if (name != null) {
			e.setName(name);
		}
		if (unit != null) {
			e.setUnit(unit);
		}
		if (minStock != null) {
			e.setMinStock(minStock);
		}
		if (cost != null) {
			e.setCost(cost);
		}
	}

}

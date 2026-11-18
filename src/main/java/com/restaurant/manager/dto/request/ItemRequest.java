package com.restaurant.manager.dto.request;

import java.util.List;
import java.util.Set;

import com.restaurant.manager.entity.Item;
import com.restaurant.manager.entity.RecipeLine;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Item. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record ItemRequest(
		@NotNull(groups = OnCreate.class) Long categoryId,
		@NotBlank(groups = OnCreate.class) @Size(max = 120) String name,
		@Size(max = 500) String description,
		@NotNull(groups = OnCreate.class) @PositiveOrZero Long price,
		@Size(max = 255) String image,
		Boolean isAvailable,
		@Min(0) @Max(3) Integer spicyLevel,
		@Positive Integer prepMinutes,
		Set<Long> tagIds,
		List<RecipeLine> recipe) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Item e) {
		if (categoryId != null) {
			e.setCategoryId(categoryId);
		}
		if (name != null) {
			e.setName(name);
		}
		if (description != null) {
			e.setDescription(description);
		}
		if (price != null) {
			e.setPrice(price);
		}
		if (image != null) {
			e.setImage(image);
		}
		if (isAvailable != null) {
			e.setIsAvailable(isAvailable);
		}
		if (spicyLevel != null) {
			e.setSpicyLevel(spicyLevel);
		}
		if (prepMinutes != null) {
			e.setPrepMinutes(prepMinutes);
		}
		if (tagIds != null) {
			e.setTagIds(tagIds);
		}
		if (recipe != null) {
			e.setRecipe(recipe);
		}
	}

}

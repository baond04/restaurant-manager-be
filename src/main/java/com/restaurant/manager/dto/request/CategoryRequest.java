package com.restaurant.manager.dto.request;


import com.restaurant.manager.entity.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Category. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record CategoryRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 80) String name,
		@PositiveOrZero Integer sortOrder,
		Boolean isActive) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Category e) {
		if (name != null) {
			e.setName(name);
		}
		if (sortOrder != null) {
			e.setSortOrder(sortOrder);
		}
		if (isActive != null) {
			e.setIsActive(isActive);
		}
	}

}

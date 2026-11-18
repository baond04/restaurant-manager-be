package com.restaurant.manager.dto.request;


import com.restaurant.manager.entity.Tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Tag. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record TagRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 60) String name,
		@NotNull(groups = OnCreate.class) @Pattern(regexp = "TASTE|INGREDIENT|DIET|OCCASION|CUISINE") String type) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Tag e) {
		if (name != null) {
			e.setName(name);
		}
		if (type != null) {
			e.setType(type);
		}
	}

}

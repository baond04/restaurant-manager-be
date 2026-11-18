package com.restaurant.manager.dto.request;


import com.restaurant.manager.entity.Floor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Floor. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record FloorRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 60) String name,
		@NotNull(groups = OnCreate.class) Integer level,
		Boolean isActive) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Floor e) {
		if (name != null) {
			e.setName(name);
		}
		if (level != null) {
			e.setLevel(level);
		}
		if (isActive != null) {
			e.setIsActive(isActive);
		}
	}

}

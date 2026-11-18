package com.restaurant.manager.dto.request;


import com.restaurant.manager.entity.Shift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Shift. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record ShiftRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 40) String name,
		@NotNull(groups = OnCreate.class) @Pattern(regexp = "\\d{2}:\\d{2}") String start,
		@NotNull(groups = OnCreate.class) @Pattern(regexp = "\\d{2}:\\d{2}") String end) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Shift e) {
		if (name != null) {
			e.setName(name);
		}
		if (start != null) {
			e.setStart(start);
		}
		if (end != null) {
			e.setEnd(end);
		}
	}

}

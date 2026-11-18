package com.restaurant.manager.dto.request;


import com.restaurant.manager.entity.DiningTable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa DiningTable. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record DiningTableRequest(
		@NotNull(groups = OnCreate.class) Long areaId,
		@NotBlank(groups = OnCreate.class) @Size(max = 10) String code,
		@NotNull(groups = OnCreate.class) @Positive Integer seats,
		Boolean isActive,
		@Pattern(regexp = "ROUND|SQUARE|RECT") String shape,
		Integer posX,
		Integer posY) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(DiningTable e) {
		if (areaId != null) {
			e.setAreaId(areaId);
		}
		if (code != null) {
			e.setCode(code);
		}
		if (seats != null) {
			e.setSeats(seats);
		}
		if (isActive != null) {
			e.setIsActive(isActive);
		}
		if (shape != null) {
			e.setShape(shape);
		}
		if (posX != null) {
			e.setPosX(posX);
		}
		if (posY != null) {
			e.setPosY(posY);
		}
	}

}

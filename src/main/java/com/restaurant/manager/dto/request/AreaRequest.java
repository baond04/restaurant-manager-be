package com.restaurant.manager.dto.request;

import java.math.BigDecimal;

import com.restaurant.manager.entity.Area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Area. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record AreaRequest(
		@NotNull(groups = OnCreate.class) Long floorId,
		@NotBlank(groups = OnCreate.class) @Size(max = 60) String name,
		@NotNull(groups = OnCreate.class) @Pattern(regexp = "HALL|OUTDOOR|VIP_ROOM|BAR") String type,
		@PositiveOrZero BigDecimal serviceFeePercent,
		@PositiveOrZero Long minSpend,
		Boolean isActive,
		@Positive Integer planW,
		@Positive Integer planH) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Area e) {
		if (floorId != null) {
			e.setFloorId(floorId);
		}
		if (name != null) {
			e.setName(name);
		}
		if (type != null) {
			e.setType(type);
		}
		if (serviceFeePercent != null) {
			e.setServiceFeePercent(serviceFeePercent);
		}
		if (minSpend != null) {
			e.setMinSpend(minSpend);
		}
		if (isActive != null) {
			e.setIsActive(isActive);
		}
		if (planW != null) {
			e.setPlanW(planW);
		}
		if (planH != null) {
			e.setPlanH(planH);
		}
	}

}

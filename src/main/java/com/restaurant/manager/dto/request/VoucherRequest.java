package com.restaurant.manager.dto.request;

import java.time.Instant;

import com.restaurant.manager.entity.Voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Voucher. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record VoucherRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 30) String code,
		@NotBlank(groups = OnCreate.class) @Size(max = 120) String name,
		@NotNull(groups = OnCreate.class) @Pattern(regexp = "PERCENT|FIXED") String type,
		@NotNull(groups = OnCreate.class) @Positive Long value,
		@PositiveOrZero Long maxDiscount,
		@PositiveOrZero Long minOrderAmount,
		@NotNull(groups = OnCreate.class) Instant startAt,
		@NotNull(groups = OnCreate.class) Instant endAt,
		@PositiveOrZero Integer usageLimit,
		Boolean isActive) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Voucher e) {
		if (code != null) {
			e.setCode(code);
		}
		if (name != null) {
			e.setName(name);
		}
		if (type != null) {
			e.setType(type);
		}
		if (value != null) {
			e.setValue(value);
		}
		if (maxDiscount != null) {
			e.setMaxDiscount(maxDiscount);
		}
		if (minOrderAmount != null) {
			e.setMinOrderAmount(minOrderAmount);
		}
		if (startAt != null) {
			e.setStartAt(startAt);
		}
		if (endAt != null) {
			e.setEndAt(endAt);
		}
		if (usageLimit != null) {
			e.setUsageLimit(usageLimit);
		}
		if (isActive != null) {
			e.setIsActive(isActive);
		}
	}

}

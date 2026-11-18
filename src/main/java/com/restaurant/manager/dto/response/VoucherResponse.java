package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.Voucher;

/** Dữ liệu Voucher trả cho client, kèm bốn trường kiểm vết. */
public record VoucherResponse(
		Long id,
		String code,
		String name,
		String type,
		Long value,
		Long maxDiscount,
		Long minOrderAmount,
		Instant startAt,
		Instant endAt,
		Integer usageLimit,
		Integer usedCount,
		Boolean isActive,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static VoucherResponse of(Voucher e) {
		return new VoucherResponse(e.getId(),
				e.getCode(),
				e.getName(),
				e.getType(),
				e.getValue(),
				e.getMaxDiscount(),
				e.getMinOrderAmount(),
				e.getStartAt(),
				e.getEndAt(),
				e.getUsageLimit(),
				e.getUsedCount(),
				e.getIsActive(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

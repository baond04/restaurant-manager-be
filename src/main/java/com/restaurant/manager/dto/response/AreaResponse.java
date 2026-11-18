package com.restaurant.manager.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.restaurant.manager.entity.Area;

/** Dữ liệu Area trả cho client, kèm bốn trường kiểm vết. */
public record AreaResponse(
		Long id,
		Long floorId,
		String name,
		String type,
		BigDecimal serviceFeePercent,
		Long minSpend,
		Boolean isActive,
		Integer planW,
		Integer planH,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static AreaResponse of(Area e) {
		return new AreaResponse(e.getId(),
				e.getFloorId(),
				e.getName(),
				e.getType(),
				e.getServiceFeePercent(),
				e.getMinSpend(),
				e.getIsActive(),
				e.getPlanW(),
				e.getPlanH(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

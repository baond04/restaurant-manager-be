package com.restaurant.manager.dto.response;

import java.time.Instant;

import com.restaurant.manager.entity.DiningTable;

/** Dữ liệu DiningTable trả cho client, kèm bốn trường kiểm vết. */
public record DiningTableResponse(
		Long id,
		Long areaId,
		String code,
		Integer seats,
		String status,
		Boolean isActive,
		String qrToken,
		String shape,
		Integer posX,
		Integer posY,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static DiningTableResponse of(DiningTable e) {
		return new DiningTableResponse(e.getId(),
				e.getAreaId(),
				e.getCode(),
				e.getSeats(),
				e.getStatus(),
				e.getIsActive(),
				e.getQrToken(),
				e.getShape(),
				e.getPosX(),
				e.getPosY(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

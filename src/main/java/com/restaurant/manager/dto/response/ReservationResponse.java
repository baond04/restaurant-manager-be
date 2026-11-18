package com.restaurant.manager.dto.response;

import java.time.Instant;
import java.util.List;

import com.restaurant.manager.entity.Reservation;

/** Dữ liệu Reservation trả cho client, kèm bốn trường kiểm vết. */
public record ReservationResponse(
		Long id,
		Long customerId,
		String guestName,
		String guestPhone,
		Instant time,
		Integer guestCount,
		Long areaId,
		String note,
		String status,
		List<Long> tableIds,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ReservationResponse of(Reservation e) {
		return new ReservationResponse(e.getId(),
				e.getCustomerId(),
				e.getGuestName(),
				e.getGuestPhone(),
				e.getTime(),
				e.getGuestCount(),
				e.getAreaId(),
				e.getNote(),
				e.getStatus(),
				e.getTableIds(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

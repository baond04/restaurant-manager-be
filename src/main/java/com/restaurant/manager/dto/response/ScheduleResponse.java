package com.restaurant.manager.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.restaurant.manager.entity.Schedule;

/** Dữ liệu Schedule trả cho client, kèm bốn trường kiểm vết. */
public record ScheduleResponse(
		Long id,
		Long employeeId,
		Long shiftId,
		Long areaId,
		LocalDate date,
		String status,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static ScheduleResponse of(Schedule e) {
		return new ScheduleResponse(e.getId(),
				e.getEmployeeId(),
				e.getShiftId(),
				e.getAreaId(),
				e.getDate(),
				e.getStatus(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

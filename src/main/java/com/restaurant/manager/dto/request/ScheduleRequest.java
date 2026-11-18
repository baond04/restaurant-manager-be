package com.restaurant.manager.dto.request;

import java.time.LocalDate;

import com.restaurant.manager.entity.Schedule;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Dữ liệu client gửi để tạo hoặc sửa Schedule. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record ScheduleRequest(
		@NotNull(groups = OnCreate.class) Long employeeId,
		@NotNull(groups = OnCreate.class) Long shiftId,
		Long areaId,
		@NotNull(groups = OnCreate.class) LocalDate date,
		@Pattern(regexp = "SCHEDULED|PRESENT|ABSENT") String status) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Schedule e) {
		if (employeeId != null) {
			e.setEmployeeId(employeeId);
		}
		if (shiftId != null) {
			e.setShiftId(shiftId);
		}
		if (areaId != null) {
			e.setAreaId(areaId);
		}
		if (date != null) {
			e.setDate(date);
		}
		if (status != null) {
			e.setStatus(status);
		}
	}

}

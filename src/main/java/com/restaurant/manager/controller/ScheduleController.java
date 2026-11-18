package com.restaurant.manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.request.ScheduleRequest;
import com.restaurant.manager.dto.request.ShiftRequest;
import com.restaurant.manager.dto.response.ScheduleResponse;
import com.restaurant.manager.dto.response.ShiftResponse;
import com.restaurant.manager.service.ScheduleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Ca làm và lịch làm")
@RestController
@RequestMapping("/api/v1")
public class ScheduleController {

	private final ScheduleService staff;

	public ScheduleController(ScheduleService staff) {
		this.staff = staff;
	}

	@GetMapping("/shifts")
	public List<ShiftResponse> shifts() {
		return staff.shifts();
	}

	@PostMapping("/shifts")
	@ResponseStatus(HttpStatus.CREATED)
	public ShiftResponse createShift(@Validated(OnCreate.class) @RequestBody ShiftRequest req) {
		return staff.createShift(req);
	}

	@PatchMapping("/shifts/{id}")
	public ShiftResponse updateShift(@PathVariable long id, @Valid @RequestBody ShiftRequest req) {
		return staff.updateShift(id, req);
	}

	@DeleteMapping("/shifts/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteShift(@PathVariable long id) {
		staff.deleteShift(id);
	}

	@GetMapping("/schedules")
	public List<ScheduleResponse> schedules() {
		return staff.schedules();
	}

	@PostMapping("/schedules")
	@ResponseStatus(HttpStatus.CREATED)
	public ScheduleResponse createSchedule(@Validated(OnCreate.class) @RequestBody ScheduleRequest req) {
		return staff.createSchedule(req);
	}

	@PatchMapping("/schedules/{id}")
	public ScheduleResponse updateSchedule(@PathVariable long id, @Valid @RequestBody ScheduleRequest req) {
		return staff.updateSchedule(id, req);
	}

	@DeleteMapping("/schedules/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSchedule(@PathVariable long id) {
		staff.deleteSchedule(id);
	}

}

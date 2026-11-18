package com.restaurant.manager.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.ScheduleRequest;
import com.restaurant.manager.dto.request.ShiftRequest;
import com.restaurant.manager.dto.response.ScheduleResponse;
import com.restaurant.manager.dto.response.ShiftResponse;
import com.restaurant.manager.entity.Schedule;
import com.restaurant.manager.entity.Shift;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.ScheduleRepository;
import com.restaurant.manager.repository.ShiftRepository;
import com.restaurant.manager.security.CurrentUser;

/** Ca làm và lịch làm. Mọi nhân viên xem được lịch (để biết ca của mình); xếp ca cần schedule:manage. */
@Service
public class ScheduleService {

	private static final Set<String> VIEW = Set.of("staff:view", "schedule:manage", "schedule:view_own");

	private final ShiftRepository shifts;
	private final ScheduleRepository schedules;

	public ScheduleService(ShiftRepository shifts, ScheduleRepository schedules) {
		this.shifts = shifts;
		this.schedules = schedules;
	}

	@Transactional(readOnly = true)
	public List<ShiftResponse> shifts() {
		CurrentUser.get().requireAny(VIEW);
		return shifts.findAll().stream().map(ShiftResponse::of).toList();
	}

	@Transactional
	public ShiftResponse createShift(ShiftRequest req) {
		CurrentUser.get().require("schedule:manage");
		var e = new Shift();
		req.applyTo(e);
		return ShiftResponse.of(shifts.save(e));
	}

	@Transactional
	public ShiftResponse updateShift(long id, ShiftRequest req) {
		CurrentUser.get().require("schedule:manage");
		Shift e = Repos.find(shifts, id);
		req.applyTo(e);
		return ShiftResponse.of(e);
	}

	@Transactional
	public void deleteShift(long id) {
		CurrentUser.get().require("schedule:manage");
		if (schedules.existsByShiftId(id)) {
			throw ApiException.conflict("Ca đang có lịch làm.");
		}
		shifts.delete(Repos.find(shifts, id));
	}

	/** Người chỉ có schedule:view_own thì chỉ thấy lịch của mình. */
	@Transactional(readOnly = true)
	public List<ScheduleResponse> schedules() {
		CurrentUser p = CurrentUser.get();
		p.requireAny(VIEW);
		boolean all = p.canAny(Set.of("staff:view", "schedule:manage"));
		return schedules.findAll().stream().filter(s -> all || s.getEmployeeId().equals(p.userId())).map(ScheduleResponse::of).toList();
	}

	@Transactional
	public ScheduleResponse createSchedule(ScheduleRequest req) {
		CurrentUser.get().require("schedule:manage");
		var e = new Schedule();
		req.applyTo(e);
		return ScheduleResponse.of(schedules.save(e));
	}

	@Transactional
	public ScheduleResponse updateSchedule(long id, ScheduleRequest req) {
		CurrentUser.get().require("schedule:manage");
		Schedule e = Repos.find(schedules, id);
		req.applyTo(e);
		return ScheduleResponse.of(e);
	}

	@Transactional
	public void deleteSchedule(long id) {
		CurrentUser.get().require("schedule:manage");
		schedules.delete(Repos.find(schedules, id));
	}

}

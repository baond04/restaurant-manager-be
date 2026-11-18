package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	boolean existsByShiftId(Long shiftId);

}

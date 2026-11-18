package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Shift;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

}

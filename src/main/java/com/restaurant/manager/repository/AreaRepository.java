package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Area;

public interface AreaRepository extends JpaRepository<Area, Long> {

	boolean existsByFloorId(Long floorId);

}

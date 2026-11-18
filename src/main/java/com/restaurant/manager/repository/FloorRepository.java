package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Floor;

public interface FloorRepository extends JpaRepository<Floor, Long> {

}

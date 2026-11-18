package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}

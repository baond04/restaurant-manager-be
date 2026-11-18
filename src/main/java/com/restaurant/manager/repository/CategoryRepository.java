package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}

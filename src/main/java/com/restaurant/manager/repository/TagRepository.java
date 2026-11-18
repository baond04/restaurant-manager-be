package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

}

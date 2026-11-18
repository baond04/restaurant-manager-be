package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

}

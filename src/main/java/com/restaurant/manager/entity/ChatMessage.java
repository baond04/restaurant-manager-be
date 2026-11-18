package com.restaurant.manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Một tin nhắn trong phiên chat; itemIds là món trợ lý đã gợi ý. */
@Getter
@Setter
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String sessionId;
	private String role;
	private String content;
	private String itemIds;
	private String intent;
	private Integer latencyMs;

}

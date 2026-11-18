package com.restaurant.manager.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Phiên chat với trợ lý; id là UUID do máy chủ sinh để client không đoán được phiên của người khác. */
@Getter
@Setter
@Entity
@Table(name = "chat_sessions")
public class ChatSession extends AuditableEntity {

	@Id
	private String id;

	private Long customerId;
	private Long orderId;
	private String title;
	private Integer turns = 0;
	private Integer feedback = 0;
	private Boolean addedToCart = false;
	private Integer latencyMs = 0;

}

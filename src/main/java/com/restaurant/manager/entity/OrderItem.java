package com.restaurant.manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Dòng món trong đơn; unitPrice là giá lúc gọi. source = CHATBOT khi khách thêm từ gợi ý AI. */
@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long orderId;
	private Long itemId;
	private Integer qty;
	private Long unitPrice;
	private String note;
	private String status = "ORDERED";
	private String source = "MENU";

}

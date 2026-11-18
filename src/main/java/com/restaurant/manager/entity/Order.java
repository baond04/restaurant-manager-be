package com.restaurant.manager.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Một lượt khách ngồi bàn (phiên bàn). Gọi thêm món chỉ thêm OrderItem. */
@Getter
@Setter
@Entity(name = "CustomerOrder")
@Table(name = "orders")
public class Order extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long tableId;
	private Long reservationId;
	private Long customerId;
	private Long openedBy;
	private String type = "DINE_IN";
	private Integer guestCount = 1;
	private String status = "OPEN";
	private String note;
	private Instant openedAt = Instant.now();
	private Instant closedAt;

}

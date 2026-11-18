package com.restaurant.manager.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Hóa đơn của một đơn; số tiền tính ở BillCalculator, không nhận từ client. */
@Getter
@Setter
@Entity
@Table(name = "bills")
public class Bill extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long orderId;
	private Long voucherId;
	private Long subtotal;
	private Long serviceFee = 0L;
	private Long minSpendTopUp = 0L;
	private Long discount = 0L;
	private Long pointsAmount = 0L;
	private Long tax = 0L;
	private Long total;
	private Integer pointsUsed = 0;
	private Integer pointsEarned = 0;
	private String method;
	private String status = "PAID";
	private Long cashierId;
	private Instant paidAt = Instant.now();

}

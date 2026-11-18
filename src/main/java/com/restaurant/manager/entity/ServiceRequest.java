package com.restaurant.manager.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Yêu cầu của khách tại bàn: gọi nhân viên, tính tiền, thêm nước, báo mở bàn. */
@Getter
@Setter
@Entity
@Table(name = "service_requests")
public class ServiceRequest extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long tableId;
	private Long orderId;
	private String type;
	private String status = "OPEN";
	private Long handledBy;
	private Instant ackAt;
	private Instant doneAt;

}

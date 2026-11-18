package com.restaurant.manager.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Mã giảm giá theo phần trăm hoặc số tiền. */
@Getter
@Setter
@Entity
@Table(name = "vouchers")
public class Voucher extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String code;
	private String name;
	private String type;
	private Long value;
	private Long maxDiscount = 0L;
	private Long minOrderAmount = 0L;
	private Instant startAt;
	private Instant endAt;
	private Integer usageLimit = 0;
	private Integer usedCount = 0;
	private Boolean isActive = true;

}

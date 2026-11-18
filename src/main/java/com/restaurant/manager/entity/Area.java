package com.restaurant.manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Khu vực trong tầng; phòng VIP có phụ thu và mức chi tối thiểu. */
@Getter
@Setter
@Entity
@Table(name = "areas")
public class Area extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long floorId;
	private String name;
	private String type;
	private BigDecimal serviceFeePercent = BigDecimal.ZERO;
	private Long minSpend = 0L;
	private Boolean isActive = true;
	@Column(name = "plan_w")
	private Integer planW = 600;
	@Column(name = "plan_h")
	private Integer planH = 400;

}

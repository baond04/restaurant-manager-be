package com.restaurant.manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Nguyên liệu và tồn kho. */
@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String unit;
	private BigDecimal stock = BigDecimal.ZERO;
	private BigDecimal minStock = BigDecimal.ZERO;
	private Long cost = 0L;

}

package com.restaurant.manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Phiếu kho: nhập, xuất, điều chỉnh, trừ theo món. */
@Getter
@Setter
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long ingredientId;
	private String type;
	private BigDecimal qty;
	private Long unitCost = 0L;
	private String note;
	private Long employeeId;

}

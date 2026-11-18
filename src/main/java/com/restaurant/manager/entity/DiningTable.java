package com.restaurant.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Bàn; qrToken in trên mã QR, đổi mã thì QR cũ hết hiệu lực. */
@Getter
@Setter
@Entity
@Table(name = "dining_tables")
public class DiningTable extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long areaId;
	private String code;
	private Integer seats;
	private String status = "AVAILABLE";
	private Boolean isActive = true;
	private String qrToken;
	private String shape = "SQUARE";
	@Column(name = "pos_x")
	private Integer posX = 0;
	@Column(name = "pos_y")
	private Integer posY = 0;

}

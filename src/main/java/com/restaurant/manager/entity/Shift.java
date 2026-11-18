package com.restaurant.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Ca làm. */
@Getter
@Setter
@Entity
@Table(name = "shifts")
public class Shift extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	@Column(name = "start_time")
	private String start;
	@Column(name = "end_time")
	private String end;

}

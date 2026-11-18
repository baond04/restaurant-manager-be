package com.restaurant.manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Đánh giá món của khách. */
@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long customerId;
	private Long itemId;
	private Integer rating;
	private String comment;
	private Boolean hidden = false;

}

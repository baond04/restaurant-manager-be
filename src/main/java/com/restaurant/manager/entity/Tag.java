package com.restaurant.manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Thẻ món (vị, thành phần, chế độ ăn, dịp, vùng miền); cũng là nút thuộc tính trong đồ thị GNN. */
@Getter
@Setter
@Entity
@Table(name = "tags")
public class Tag extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String type;

}

package com.restaurant.manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một dòng công thức: lượng nguyên liệu cho một phần món; dùng để trừ kho khi gọi món. */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class RecipeLine {

	private Long ingredientId;
	private BigDecimal qty;

}

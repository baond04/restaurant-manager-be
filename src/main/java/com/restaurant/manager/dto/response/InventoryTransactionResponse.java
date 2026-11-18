package com.restaurant.manager.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.restaurant.manager.entity.InventoryTransaction;

/** Dữ liệu InventoryTransaction trả cho client, kèm bốn trường kiểm vết. */
public record InventoryTransactionResponse(
		Long id,
		Long ingredientId,
		String type,
		BigDecimal qty,
		Long unitCost,
		String note,
		Long employeeId,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static InventoryTransactionResponse of(InventoryTransaction e) {
		return new InventoryTransactionResponse(e.getId(),
				e.getIngredientId(),
				e.getType(),
				e.getQty(),
				e.getUnitCost(),
				e.getNote(),
				e.getEmployeeId(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

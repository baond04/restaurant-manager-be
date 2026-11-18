package com.restaurant.manager.dto.response;

import java.time.Instant;
import java.util.Set;

import com.restaurant.manager.entity.Role;

/** Dữ liệu Role trả cho client, kèm bốn trường kiểm vết. */
public record RoleResponse(
		Long id,
		String code,
		String name,
		String description,
		Boolean isSystem,
		Boolean locked,
		Boolean fixed,
		Set<String> permissions,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static RoleResponse of(Role e) {
		return new RoleResponse(e.getId(),
				e.getCode(),
				e.getName(),
				e.getDescription(),
				e.getIsSystem(),
				e.getLocked(),
				e.getFixed(),
				e.getPermissions(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

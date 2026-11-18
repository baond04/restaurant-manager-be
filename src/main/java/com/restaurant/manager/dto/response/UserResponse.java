package com.restaurant.manager.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import com.restaurant.manager.entity.User;

/** Dữ liệu User trả cho client, kèm bốn trường kiểm vết. */
public record UserResponse(
		Long id,
		String kind,
		Long roleId,
		String fullName,
		String position,
		String email,
		String phone,
		String status,
		String workStatus,
		LocalDate hireDate,
		String gender,
		Integer points,
		Instant lastLoginAt,
		Set<Long> likeTagIds,
		Set<Long> allergyTagIds,
		Instant createdAt,
		Long createdBy,
		Instant updatedAt,
		Long updatedBy) {

	public static UserResponse of(User e) {
		return new UserResponse(e.getId(),
				e.getKind(),
				e.getRoleId(),
				e.getFullName(),
				e.getPosition(),
				e.getEmail(),
				e.getPhone(),
				e.getStatus(),
				e.getWorkStatus(),
				e.getHireDate(),
				e.getGender(),
				e.getPoints(),
				e.getLastLoginAt(),
				e.getLikeTagIds(),
				e.getAllergyTagIds(),
				e.getCreatedAt(),
				e.getCreatedBy(),
				e.getUpdatedAt(),
				e.getUpdatedBy());
	}

}

package com.restaurant.manager.dto.request;

import java.time.LocalDate;
import java.util.Set;

import com.restaurant.manager.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa User. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record UserRequest(
		@NotNull(groups = OnCreate.class) @Pattern(regexp = "STAFF|CUSTOMER") String kind,
		Long roleId,
		@NotBlank(groups = OnCreate.class) @Size(max = 100) String fullName,
		@Size(max = 60) String position,
		@Email @Size(max = 120) String email,
		@Pattern(regexp = "0\\d{9}") String phone,
		@Size(min = 6, max = 72) String password,
		@Pattern(regexp = "ACTIVE|LOCKED") String status,
		@Pattern(regexp = "WORKING|ON_LEAVE|QUIT") String workStatus,
		LocalDate hireDate,
		String gender,
		Set<Long> likeTagIds,
		Set<Long> allergyTagIds) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(User e) {
		if (kind != null) {
			e.setKind(kind);
		}
		if (roleId != null) {
			e.setRoleId(roleId);
		}
		if (fullName != null) {
			e.setFullName(fullName);
		}
		if (position != null) {
			e.setPosition(position);
		}
		if (email != null) {
			e.setEmail(email);
		}
		if (phone != null) {
			e.setPhone(phone);
		}
		if (status != null) {
			e.setStatus(status);
		}
		if (workStatus != null) {
			e.setWorkStatus(workStatus);
		}
		if (hireDate != null) {
			e.setHireDate(hireDate);
		}
		if (gender != null) {
			e.setGender(gender);
		}
		if (likeTagIds != null) {
			e.setLikeTagIds(likeTagIds);
		}
		if (allergyTagIds != null) {
			e.setAllergyTagIds(allergyTagIds);
		}
	}

}

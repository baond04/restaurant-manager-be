package com.restaurant.manager.dto.request;

import java.util.Set;

import com.restaurant.manager.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Role. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record RoleRequest(
		@NotBlank(groups = OnCreate.class) @Size(max = 40) String code,
		@NotBlank(groups = OnCreate.class) @Size(max = 100) String name,
		@Size(max = 255) String description,
		Set<String> permissions) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Role e) {
		if (code != null) {
			e.setCode(code);
		}
		if (name != null) {
			e.setName(name);
		}
		if (description != null) {
			e.setDescription(description);
		}
		if (permissions != null) {
			e.setPermissions(permissions);
		}
	}

}

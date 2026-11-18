package com.restaurant.manager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Khách tự đăng ký tài khoản. */
public record RegisterRequest(
		@NotBlank @Size(max = 100) String fullName,
		@NotBlank @Email String email,
		@NotBlank @Pattern(regexp = "0\\d{9}", message = "phải là 10 số, bắt đầu bằng 0") String phone,
		@NotBlank @Size(min = 6, max = 72) String password) {

}

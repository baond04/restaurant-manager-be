package com.restaurant.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Đăng nhập bằng email hoặc số điện thoại; kind cho biết đăng nhập ở cổng nhân viên hay cổng khách. */
public record LoginRequest(@NotBlank String login, @NotBlank String password, @NotBlank @Pattern(regexp = "STAFF|CUSTOMER") String kind) {

}

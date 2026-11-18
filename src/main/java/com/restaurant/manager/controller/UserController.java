package com.restaurant.manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.request.UserRequest;
import com.restaurant.manager.dto.response.UserResponse;
import com.restaurant.manager.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Người dùng")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService users;

	public UserController(UserService users) {
		this.users = users;
	}

	@Operation(summary = "Nhân viên thấy mọi tài khoản; khách chỉ thấy tài khoản của mình")
	@GetMapping
	public List<UserResponse> list() {
		return users.list();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse create(@Validated(OnCreate.class) @RequestBody UserRequest req) {
		return users.create(req);
	}

	@Operation(summary = "Sửa từng phần; khách chỉ tự sửa hồ sơ, sở thích, dị ứng của mình")
	@PatchMapping("/{id}")
	public UserResponse update(@PathVariable long id, @Valid @RequestBody UserRequest req) {
		return users.update(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		users.delete(id);
	}

}

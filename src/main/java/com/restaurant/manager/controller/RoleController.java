package com.restaurant.manager.controller;

import java.util.List;
import java.util.Map;

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
import com.restaurant.manager.dto.request.RoleRequest;
import com.restaurant.manager.dto.response.RoleResponse;
import com.restaurant.manager.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Vai trò và quyền")
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

	private final RoleService roles;

	public RoleController(RoleService roles) {
		this.roles = roles;
	}

	@GetMapping
	public List<RoleResponse> list() {
		return roles.list();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RoleResponse create(@Validated(OnCreate.class) @RequestBody RoleRequest req) {
		return roles.create(req);
	}

	@PatchMapping("/{id}")
	public RoleResponse update(@PathVariable long id, @Valid @RequestBody RoleRequest req) {
		return roles.update(id, req);
	}

	@Operation(summary = "Nhân bản một vai trò (kèm quyền) để tạo vai trò mới")
	@PostMapping("/{id}/clone")
	@ResponseStatus(HttpStatus.CREATED)
	public RoleResponse cloneRole(@PathVariable long id, @RequestBody Map<String, String> body) {
		return roles.cloneRole(id, body.get("code"), body.get("name"));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		roles.delete(id);
	}

}

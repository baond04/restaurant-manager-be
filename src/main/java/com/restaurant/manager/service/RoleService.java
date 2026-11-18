package com.restaurant.manager.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.RoleRequest;
import com.restaurant.manager.dto.response.RoleResponse;
import com.restaurant.manager.entity.Role;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.RoleRepository;
import com.restaurant.manager.repository.UserRepository;
import com.restaurant.manager.security.CurrentUser;

/** Vai trò và quyền. Quản trị luôn có mọi quyền, Khách hàng có quyền cố định: hai vai trò này không sửa được. */
@Service
public class RoleService {

	private final RoleRepository roles;
	private final UserRepository users;

	public RoleService(RoleRepository roles, UserRepository users) {
		this.roles = roles;
		this.users = users;
	}

	@Transactional(readOnly = true)
	public List<RoleResponse> list() {
		CurrentUser.get().requireAny(Set.of("role:manage", "user:manage", "staff:view"));
		return roles.findAll().stream().map(RoleResponse::of).toList();
	}

	@Transactional
	public RoleResponse create(RoleRequest req) {
		CurrentUser.get().require("role:manage");
		if (roles.findByCode(req.code()).isPresent()) {
			throw ApiException.conflict("Mã vai trò " + req.code() + " đã tồn tại");
		}
		var e = new Role();
		req.applyTo(e);
		return RoleResponse.of(roles.save(e));
	}

	@Transactional
	public RoleResponse update(long id, RoleRequest req) {
		CurrentUser.get().require("role:manage");
		Role e = Repos.find(roles, id);
		if (Boolean.TRUE.equals(e.getLocked()) || Boolean.TRUE.equals(e.getFixed())) {
			throw ApiException.badRequest("Vai trò " + e.getName() + " không sửa được");
		}
		req.applyTo(e);
		return RoleResponse.of(e);
	}

	@Transactional
	public RoleResponse cloneRole(long fromId, String code, String name) {
		CurrentUser.get().require("role:manage");
		Role from = Repos.find(roles, fromId);
		return create(new RoleRequest(code, name, "Nhân bản từ " + from.getName(), new HashSet<>(from.getPermissions())));
	}

	@Transactional
	public void delete(long id) {
		CurrentUser.get().require("role:manage");
		Role r = Repos.find(roles, id);
		if (Boolean.TRUE.equals(r.getIsSystem())) {
			throw ApiException.conflict("Vai trò hệ thống không xóa được.");
		}
		if (users.existsByRoleId(id)) {
			throw ApiException.conflict("Vai trò đang có người dùng.");
		}
		roles.delete(r);
	}

}

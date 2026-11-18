package com.restaurant.manager.service;

import java.time.Instant;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.LoginRequest;
import com.restaurant.manager.dto.request.RegisterRequest;
import com.restaurant.manager.dto.response.AuthResponse;
import com.restaurant.manager.dto.response.UserResponse;
import com.restaurant.manager.entity.User;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.RoleRepository;
import com.restaurant.manager.repository.UserRepository;
import com.restaurant.manager.security.JwtService;
import com.restaurant.manager.security.TokenStore;

/** Đăng nhập, đăng ký, làm mới và hủy phiên. Sai 5 lần trong 15 phút thì khóa tạm. Bếp, phục vụ (không có vai trò) không đăng nhập được. */
@Service
public class AuthService {

	private final UserRepository users;
	private final RoleRepository roles;
	private final PasswordEncoder encoder;
	private final JwtService jwt;
	private final TokenStore tokens;

	public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder encoder, JwtService jwt, TokenStore tokens) {
		this.users = users;
		this.roles = roles;
		this.encoder = encoder;
		this.jwt = jwt;
		this.tokens = tokens;
	}

	/** Kết quả gồm refresh token để controller đặt vào cookie. */
	public record Session(AuthResponse body, String refreshToken) {
	}

	@Transactional
	public Session login(LoginRequest req) {
		String key = req.login().trim().toLowerCase();
		tokens.checkNotLocked(key);
		User u = (key.contains("@") ? users.findByEmailIgnoreCase(key) : users.findByPhone(key)).orElse(null);
		boolean ok = u != null && u.getPasswordHash() != null && u.getKind().equals(req.kind()) && encoder.matches(req.password(), u.getPasswordHash())
				&& ("CUSTOMER".equals(u.getKind()) || u.getRoleId() != null);
		if (!ok) {
			tokens.recordFail(key);
			throw ApiException.unauthorized("Sai tài khoản hoặc mật khẩu");
		}
		if ("LOCKED".equals(u.getStatus())) {
			throw ApiException.forbidden();
		}
		tokens.clearFails(key);
		u.setLastLoginAt(Instant.now());
		return session(u);
	}

	@Transactional
	public Session register(RegisterRequest req) {
		if (users.existsByEmailIgnoreCase(req.email())) {
			throw ApiException.conflict("Email đã được dùng");
		}
		if (users.existsByPhone(req.phone())) {
			throw ApiException.conflict("Số điện thoại đã được dùng");
		}
		var u = new User();
		u.setKind("CUSTOMER");
		u.setRoleId(roles.findByCode("CUSTOMER").map(r -> r.getId()).orElse(null));
		u.setFullName(req.fullName().trim());
		u.setEmail(req.email().trim().toLowerCase());
		u.setPhone(req.phone());
		u.setPasswordHash(encoder.encode(req.password()));
		return session(users.save(u));
	}

	/** Đổi refresh token cũ lấy cặp token mới (token cũ bị hủy). */
	@Transactional(readOnly = true)
	public Session refresh(String refreshToken) {
		User u = users.findById(tokens.consume(refreshToken)).orElseThrow(() -> ApiException.unauthorized("Tài khoản không còn"));
		if ("LOCKED".equals(u.getStatus())) {
			throw ApiException.forbidden();
		}
		return session(u);
	}

	public void logout(String refreshToken) {
		tokens.revoke(refreshToken);
	}

	@Transactional(readOnly = true)
	public AuthResponse me(long userId) {
		User u = users.findById(userId).orElseThrow(() -> ApiException.unauthorized("Tài khoản không còn"));
		return new AuthResponse(null, UserResponse.of(u), permissions(u));
	}

	private Session session(User u) {
		Set<String> perms = permissions(u);
		return new Session(new AuthResponse(jwt.accessToken(u.getId(), u.getKind(), perms), UserResponse.of(u), perms), tokens.issue(u.getId()));
	}

	private Set<String> permissions(User u) {
		if (u.getRoleId() == null || "CUSTOMER".equals(u.getKind())) {
			return Set.of();
		}
		return roles.findById(u.getRoleId()).map(r -> Set.copyOf(r.getPermissions())).orElse(Set.of());
	}

}

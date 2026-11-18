package com.restaurant.manager.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.LoginRequest;
import com.restaurant.manager.dto.request.RegisterRequest;
import com.restaurant.manager.dto.response.AuthResponse;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.security.CurrentUser;
import com.restaurant.manager.security.TokenStore;
import com.restaurant.manager.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Xác thực")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private static final String COOKIE = "rt";

	private final AuthService auth;

	public AuthController(AuthService auth) {
		this.auth = auth;
	}

	@Operation(summary = "Đăng nhập bằng email hoặc số điện thoại")
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
		return withCookie(auth.login(req));
	}

	@Operation(summary = "Khách tự đăng ký tài khoản")
	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
		return withCookie(auth.register(req));
	}

	@Operation(summary = "Đổi refresh token trong cookie lấy access token mới")
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@CookieValue(name = COOKIE, required = false) String token) {
		return withCookie(auth.refresh(token));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = COOKIE, required = false) String token) {
		auth.logout(token);
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", 0).toString()).build();
	}

	@Operation(summary = "Người đang đăng nhập và quyền")
	@GetMapping("/me")
	public AuthResponse me() {
		Long id = CurrentUser.get().userId();
		if (id == null) {
			throw ApiException.unauthorized("Chưa đăng nhập");
		}
		return auth.me(id);
	}

	private static ResponseEntity<AuthResponse> withCookie(AuthService.Session s) {
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie(s.refreshToken(), TokenStore.REFRESH_TTL.toSeconds()).toString()).body(s.body());
	}

	private static ResponseCookie cookie(String value, long maxAge) {
		return ResponseCookie.from(COOKIE, value).httpOnly(true).secure(true).sameSite("Strict").path("/api/v1/auth").maxAge(maxAge).build();
	}

}

package com.restaurant.manager.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.restaurant.manager.exception.ApiException;

/**
 * Refresh token: chuỗi ngẫu nhiên 256 bit, Redis chỉ giữ SHA-256 của nó (lộ Redis cũng không dùng lại được token).
 * Mỗi lần refresh xóa token cũ và cấp token mới (xoay vòng). Đếm đăng nhập sai để chặn dò mật khẩu.
 */
@Component
public class TokenStore {

	public static final Duration REFRESH_TTL = Duration.ofDays(7);
	private static final int MAX_FAILS = 5;
	private static final Duration FAIL_WINDOW = Duration.ofMinutes(15);

	private final StringRedisTemplate redis;
	private final SecureRandom random = new SecureRandom();

	public TokenStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	public String issue(long userId) {
		byte[] raw = new byte[32];
		random.nextBytes(raw);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
		redis.opsForValue().set("rt:" + sha256(token), String.valueOf(userId), REFRESH_TTL);
		return token;
	}

	/** Trả userId và xóa token cũ; token không còn (hết hạn, đã dùng, đã đăng xuất) thì 401. */
	public long consume(String token) {
		String userId = token == null ? null : redis.opsForValue().getAndDelete("rt:" + sha256(token));
		if (userId == null) {
			throw ApiException.unauthorized("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
		}
		return Long.parseLong(userId);
	}

	public void revoke(String token) {
		if (token != null) {
			redis.delete("rt:" + sha256(token));
		}
	}

	public void checkNotLocked(String key) {
		String n = redis.opsForValue().get("rl:login:" + key);
		if (n != null && Integer.parseInt(n) >= MAX_FAILS) {
			throw new ApiException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Sai quá 5 lần, thử lại sau 15 phút");
		}
	}

	public void recordFail(String key) {
		Long n = redis.opsForValue().increment("rl:login:" + key);
		if (n != null && n == 1) {
			redis.expire("rl:login:" + key, FAIL_WINDOW);
		}
	}

	public void clearFails(String key) {
		redis.delete("rl:login:" + key);
	}

	static String sha256(String s) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}

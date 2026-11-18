package com.restaurant.manager.security;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.restaurant.manager.exception.ApiException;

/**
 * Người đang gọi API, đọc từ JWT. kind: STAFF, CUSTOMER hoặc GUEST (khách vãng lai đã vào bàn bằng QR).
 * Khách vãng lai không có userId, chỉ có orderId và tableId của phiên bàn.
 */
public record CurrentUser(Long userId, String kind, Set<String> perms, Long orderId, Long tableId) {

	public static final CurrentUser ANONYMOUS = new CurrentUser(null, "ANONYMOUS", Set.of(), null, null);

	public static CurrentUser get() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
			return ANONYMOUS;
		}
		List<String> perms = jwt.getClaimAsStringList("perms");
		return new CurrentUser(jwt.getSubject().startsWith("guest") ? null : Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("kind"),
				perms == null ? Set.of() : Set.copyOf(perms), longClaim(jwt, "oid"), longClaim(jwt, "tid"));
	}

	private static Long longClaim(Jwt jwt, String name) {
		Object v = jwt.getClaim(name);
		return v == null ? null : ((Number) v).longValue();
	}

	public boolean isStaff() {
		return "STAFF".equals(kind);
	}

	public boolean isCustomer() {
		return "CUSTOMER".equals(kind);
	}

	public boolean isGuest() {
		return "GUEST".equals(kind);
	}

	public boolean can(String perm) {
		return perms.contains(perm);
	}

	public boolean canAny(Set<String> any) {
		return any.stream().anyMatch(perms::contains);
	}

	public void require(String perm) {
		if (!can(perm)) {
			throw ApiException.forbidden();
		}
	}

	public void requireAny(Set<String> any) {
		if (!canAny(any)) {
			throw ApiException.forbidden();
		}
	}

}

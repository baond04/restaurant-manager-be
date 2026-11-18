package com.restaurant.manager.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/** Ký và kiểm JWT HS256. Access token 15 phút; token khách tại bàn 4 giờ và bị từ chối ngay khi đơn đóng. */
@Service
public class JwtService {

	static final Duration ACCESS_TTL = Duration.ofMinutes(15);
	static final Duration GUEST_TTL = Duration.ofHours(4);

	private final SecretKey key;
	private final JwtEncoder encoder;

	public JwtService(@Value("${app.jwt.secret}") String secret) {
		if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("app.jwt.secret phải dài ít nhất 32 byte");
		}
		this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	public JwtDecoder decoder() {
		return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
	}

	/** Token của nhân viên hoặc khách có tài khoản; quyền nằm trong claim perms. */
	public String accessToken(long userId, String kind, Iterable<String> perms) {
		return sign(String.valueOf(userId), Map.of("kind", kind, "perms", perms), ACCESS_TTL);
	}

	/** Token khách vãng lai tại bàn: gắn với một phiên bàn (đơn đang mở). */
	public String guestToken(long orderId, long tableId) {
		return sign("guest-" + UUID.randomUUID(), Map.of("kind", "GUEST", "oid", orderId, "tid", tableId, "perms", new String[0]), GUEST_TTL);
	}

	private String sign(String subject, Map<String, Object> claims, Duration ttl) {
		Instant now = Instant.now();
		var set = JwtClaimsSet.builder().subject(subject).issuedAt(now).expiresAt(now.plus(ttl)).id(UUID.randomUUID().toString())
				.claims(c -> c.putAll(claims)).build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), set)).getTokenValue();
	}

}

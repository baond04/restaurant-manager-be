package com.restaurant.manager.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.restaurant.manager.security.JwtService;

/**
 * API không trạng thái: access token trong header Bearer, refresh token trong cookie HttpOnly (chỉ /api/v1/auth nhận).
 * Quyền chi tiết kiểm ở tầng service (CurrentUser.require) vì một số tài nguyên đọc công khai nhưng ẩn bớt trường hoặc dòng.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] PUBLIC = {
			"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/actuator/info",
			"/api/v1/auth/**", "/api/v1/table-sessions/**", "/api/v1/chat", "/api/v1/chat/*/feedback", "/api/v1/ai/model" };
	/** Đọc không cần đăng nhập; service che trường nhạy cảm (mã QR, công thức) theo người gọi. */
	private static final String[] PUBLIC_READ = {
			"/api/v1/categories", "/api/v1/tags", "/api/v1/items", "/api/v1/floors", "/api/v1/areas", "/api/v1/tables",
			"/api/v1/reviews", "/api/v1/reservations/availability" };

	@Bean
	SecurityFilterChain api(HttpSecurity http, JwtDecoder decoder) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable) // Bearer token; cookie refresh chỉ nhận ở /auth với SameSite=Strict
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						.requestMatchers(PUBLIC).permitAll()
						.requestMatchers(HttpMethod.GET, PUBLIC_READ).permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/reservations").permitAll() // khách không tài khoản đặt bàn
						.anyRequest().authenticated())
				.oauth2ResourceServer(o -> o.jwt(j -> j.decoder(decoder).jwtAuthenticationConverter(converter())))
				.build();
	}

	private static JwtAuthenticationConverter converter() {
		var perms = new JwtGrantedAuthoritiesConverter();
		perms.setAuthoritiesClaimName("perms");
		perms.setAuthorityPrefix("");
		var c = new JwtAuthenticationConverter();
		c.setJwtGrantedAuthoritiesConverter(perms);
		return c;
	}

	@Bean
	JwtDecoder jwtDecoder(JwtService jwt) {
		return jwt.decoder();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(11);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origins) {
		var cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(origins);
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setAllowCredentials(true); // để trình duyệt gửi cookie refresh token
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}

}

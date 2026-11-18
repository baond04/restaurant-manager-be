package com.restaurant.manager.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.restaurant.manager.security.CurrentUser;

/** Điền created_by, updated_by bằng id người đang đăng nhập (lấy từ JWT). */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

	@Bean
	AuditorAware<Long> auditorAware() {
		return () -> Optional.ofNullable(CurrentUser.get().userId());
	}

}

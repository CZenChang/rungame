package com.dodognoman.rungame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;


@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
@SpringBootApplication
public class RungameApplication {

	public static void main(String[] args) {
		SpringApplication.run(RungameApplication.class, args);
	}

	/**
	 * Auditing（@CreatedDate / @LastModifiedDate）預設回傳 LocalDateTime，
	 * 無法塞進 OffsetDateTime 欄位，故自訂提供者回傳 UTC 的 OffsetDateTime。
	 */
	@Bean
	DateTimeProvider offsetDateTimeProvider() {
		return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
	}

}

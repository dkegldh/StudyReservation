package com.jin.studyreservation.global.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

  /** createdAt, updatedAt도 서비스와 같은 Clock(Asia/Seoul) 기준으로 기록 */
  @Bean
  public DateTimeProvider auditingDateTimeProvider(Clock clock) {
    return () -> Optional.of(LocalDateTime.now(clock));
  }
}

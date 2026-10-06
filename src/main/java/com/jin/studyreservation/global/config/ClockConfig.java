package com.jin.studyreservation.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API의 날짜는 타임존 없는 LocalDateTime이므로, 서버 OS 타임존과 무관하게 한국 시간으로 고정한다.
 * 테스트에서는 고정 Clock으로 바꿔 끼울 수 있다.
 */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of("Asia/Seoul"));
  }
}

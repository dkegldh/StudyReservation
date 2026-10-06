package com.jin.studyreservation.support;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Postgres, Redis 컨테이너를 JVM 당 한 번만 띄워 모든 통합 테스트가 공유한다.
 * 동시성 테스트는 여러 스레드의 커밋을 봐야 하므로 @Transactional 롤백 대신 매 테스트 후 테이블을 비운다.
 */
@SpringBootTest
public abstract class IntegrationTestSupport {

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

  static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(6379);

  static {
    POSTGRES.start();
    REDIS.start();
  }

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
  }

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterEach
  void truncateTables() {
    jdbcTemplate.execute(
        "TRUNCATE TABLE bookmarks, comments, participations, meetings, users RESTART IDENTITY CASCADE");
  }
}

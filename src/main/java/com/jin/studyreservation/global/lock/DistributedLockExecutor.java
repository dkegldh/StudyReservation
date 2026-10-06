package com.jin.studyreservation.global.lock;

import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * Redisson 분산 락 안에서 작업을 실행한다.
 * <p>
 * 락 획득 → 트랜잭션 시작 → 커밋 → 락 해제 순서를 지키기 위해, 이 메서드를 호출하는 쪽은 트랜잭션 밖에 있어야 하고
 * action 안에서 {@code @Transactional} 서비스 메서드를 호출해야 한다.
 * leaseTime을 지정하지 않아 watchdog이 락을 연장하므로, 트랜잭션이 길어져도 커밋 전에 락이 풀리지 않는다.
 * watchdog 기본 TTL(30초)이 키에 걸리므로 프로세스가 죽어도 락이 영구히 남지 않는다.
 */
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

  private static final long WAIT_SECONDS = 5;

  private final RedissonClient redissonClient;

  public <T> T execute(String key, Supplier<T> action) {
    RLock lock = redissonClient.getLock(key);
    boolean acquired;
    try {
      acquired = lock.tryLock(WAIT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BusinessException(ErrorCode.LOCK_TIMEOUT);
    }
    if (!acquired) {
      throw new BusinessException(ErrorCode.LOCK_TIMEOUT);
    }
    try {
      return action.get();
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  public void execute(String key, Runnable action) {
    execute(key, () -> {
      action.run();
      return null;
    });
  }
}

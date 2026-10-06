package com.jin.studyreservation.domain.meeting.service;

import com.jin.studyreservation.domain.meeting.dto.MeetingRequest;
import com.jin.studyreservation.global.lock.DistributedLockExecutor;
import com.jin.studyreservation.global.lock.LockKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 모임 수정, 취소, 삭제를 신청/취소와 같은 락으로 직렬화한다.
 * Hibernate는 변경된 엔티티의 모든 컬럼을 UPDATE하므로, 락 없이 수정하면 동시에 들어온 신청의
 * current_participants 증가분을 오래된 값으로 덮어쓸 수 있다.
 * 트랜잭션이 락 안에서 끝나야 하므로 이 클래스에는 {@code @Transactional}을 붙이지 않는다.
 */
@Component
@RequiredArgsConstructor
public class MeetingFacade {

  private final DistributedLockExecutor lockExecutor;
  private final MeetingService meetingService;

  public void update(Long meetingId, Long userId, MeetingRequest request) {
    lockExecutor.execute(LockKeys.meeting(meetingId),
        () -> meetingService.update(meetingId, userId, request));
  }

  public void cancel(Long meetingId, Long userId) {
    lockExecutor.execute(LockKeys.meeting(meetingId),
        () -> meetingService.cancel(meetingId, userId));
  }

  public void delete(Long meetingId, Long userId) {
    lockExecutor.execute(LockKeys.meeting(meetingId),
        () -> meetingService.delete(meetingId, userId));
  }
}

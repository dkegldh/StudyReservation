package com.jin.studyreservation.domain.participation.service;

import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import com.jin.studyreservation.global.lock.DistributedLockExecutor;
import com.jin.studyreservation.global.lock.LockKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * 신청/취소의 진입점. 락 획득 → 트랜잭션 시작 → 커밋 → 락 해제 순서를 지키기 위해
 * 이 클래스에는 {@code @Transactional}을 붙이지 않는다.
 */
@Component
@RequiredArgsConstructor
public class ParticipationFacade {

  private static final String DUPLICATE_CONSTRAINT = "uk_participations_meeting_user";

  private final DistributedLockExecutor lockExecutor;
  private final ParticipationService participationService;

  public void apply(Long meetingId, Long userId) {
    try {
      lockExecutor.execute(LockKeys.meeting(meetingId),
          () -> participationService.apply(meetingId, userId));
    } catch (DataIntegrityViolationException e) {
      // 락이 정상이라면 도달하지 않는다. 유니크 제약(meeting_id, user_id)이 최종 방어선으로 막은 경우만 변환하고,
      // 정원 CHECK 제약 위반 등 다른 무결성 오류는 버그 신호이므로 그대로 던진다
      if (isDuplicateParticipation(e)) {
        throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING);
      }
      throw e;
    }
  }

  private boolean isDuplicateParticipation(DataIntegrityViolationException e) {
    String message = e.getMostSpecificCause().getMessage();
    return message != null && message.contains(DUPLICATE_CONSTRAINT);
  }

  public void cancel(Long meetingId, Long userId) {
    lockExecutor.execute(LockKeys.meeting(meetingId),
        () -> participationService.cancel(meetingId, userId));
  }
}

package com.jin.studyreservation.global.lock;

public final class LockKeys {

  private LockKeys() {
  }

  /** 신청/취소와 모임 수정·취소·삭제가 같은 락을 공유해 current_participants 갱신을 직렬화한다 */
  public static String meeting(Long meetingId) {
    return "lock:meeting:" + meetingId;
  }
}

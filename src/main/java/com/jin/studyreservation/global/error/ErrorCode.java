package com.jin.studyreservation.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * message는 화면에 그대로 노출되므로 해요체로 쓴다.
 * code는 프론트 목 API(mock.ts)와 같은 값을 쓴다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 공통
  INVALID_INPUT(400, "입력값을 다시 확인해 주세요."),
  UNAUTHORIZED(401, "로그인이 필요해요."),
  FORBIDDEN(403, "권한이 없어요."),
  LOCK_TIMEOUT(429, "요청이 몰리고 있어요. 잠시 후 다시 시도해 주세요."),
  INTERNAL_ERROR(500, "잠시 후 다시 시도해 주세요."),

  // 사용자
  USER_NOT_FOUND(404, "사용자를 찾을 수 없어요."),

  // 모임
  MEETING_NOT_FOUND(404, "모임을 찾을 수 없어요."),
  NOT_MEETING_HOST(403, "모임장만 할 수 있어요."),
  INVALID_MEETING_SCHEDULE(400, "모집 마감은 지금 이후, 모임 시작 이전이어야 해요."),
  INVALID_MAX_PARTICIPANTS(400, "정원은 현재 참여 인원보다 적게 줄일 수 없어요."),
  MEETING_ALREADY_CANCELED(400, "이미 취소된 모임이에요."),
  MEETING_ALREADY_STARTED(400, "이미 시작된 모임이에요."),
  MEETING_HAS_PARTICIPANTS(409, "참여자가 있는 모임은 삭제할 수 없어요. 모임 취소를 이용해 주세요."),

  // 신청
  RECRUITMENT_CLOSED(400, "모집이 마감된 모임이에요."),
  MEETING_FULL(409, "방금 정원이 모두 찼어요."),
  HOST_CANNOT_PARTICIPATE(400, "내가 만든 모임에는 신청할 수 없어요."),
  ALREADY_PARTICIPATING(409, "이미 신청한 모임이에요."),
  NOT_PARTICIPATING(400, "신청하지 않은 모임이에요."),

  // 댓글
  COMMENT_NOT_FOUND(404, "댓글을 찾을 수 없어요."),
  NOT_COMMENT_AUTHOR(403, "내가 쓴 댓글만 삭제할 수 있어요."),
  INVALID_PARENT_COMMENT(400, "답글을 달 수 없는 댓글이에요."),
  COMMENT_ALREADY_DELETED(400, "이미 삭제된 댓글이에요.");

  /** HTTP 상태 코드. web 의존성이 아직 없어 int로 두고, 예외 처리기에서 HttpStatus로 변환한다 */
  private final int status;
  private final String message;
}

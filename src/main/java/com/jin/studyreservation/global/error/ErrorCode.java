package com.jin.studyreservation.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * message는 화면에 그대로 노출되므로 해요체로 쓴다.
 * code는 프론트 목 API(mock.ts)와 같은 값을 쓴다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 공통
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 다시 확인해 주세요."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요해요."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없어요."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없어요."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청이에요."),
  LOCK_TIMEOUT(HttpStatus.TOO_MANY_REQUESTS, "요청이 몰리고 있어요. 잠시 후 다시 시도해 주세요."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해 주세요."),

  // 사용자
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없어요."),

  // 모임
  MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없어요."),
  NOT_MEETING_HOST(HttpStatus.FORBIDDEN, "모임장만 할 수 있어요."),
  INVALID_MEETING_SCHEDULE(HttpStatus.BAD_REQUEST, "모집 마감은 지금 이후, 모임 시작 이전이어야 해요."),
  INVALID_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "정원은 현재 참여 인원보다 적게 줄일 수 없어요."),
  MEETING_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 모임이에요."),
  MEETING_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "이미 시작된 모임이에요."),
  MEETING_HAS_PARTICIPANTS(HttpStatus.CONFLICT, "참여자가 있는 모임은 삭제할 수 없어요. 모임 취소를 이용해 주세요."),

  // 신청
  RECRUITMENT_CLOSED(HttpStatus.BAD_REQUEST, "모집이 마감된 모임이에요."),
  MEETING_FULL(HttpStatus.CONFLICT, "방금 정원이 모두 찼어요."),
  HOST_CANNOT_PARTICIPATE(HttpStatus.BAD_REQUEST, "내가 만든 모임에는 신청할 수 없어요."),
  ALREADY_PARTICIPATING(HttpStatus.CONFLICT, "이미 신청한 모임이에요."),
  NOT_PARTICIPATING(HttpStatus.BAD_REQUEST, "신청하지 않은 모임이에요."),

  // 댓글
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없어요."),
  NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "내가 쓴 댓글만 삭제할 수 있어요."),
  INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "답글을 달 수 없는 댓글이에요."),
  COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 댓글이에요.");

  private final HttpStatus status;
  private final String message;
}

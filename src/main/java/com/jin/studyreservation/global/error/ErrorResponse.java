package com.jin.studyreservation.global.error;

/** 모든 에러 응답의 형식. 프론트 client.ts가 code, message를 읽는다 */
public record ErrorResponse(String code, String message) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.name(), errorCode.getMessage());
  }
}

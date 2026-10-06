package com.jin.studyreservation.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 모든 예외를 {@code { code, message }} 형식으로 응답한다.
 * message는 화면에 그대로 노출되므로, 내부 예외 메시지(영문, 스택 정보)를 응답에 담지 않는다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    log.debug("Business exception: {}", e.getErrorCode());
    return toResponse(e.getErrorCode());
  }

  /** 요청 본문(@Valid @RequestBody), 파라미터 검증 실패, 잘못된 JSON·enum·날짜 형식 */
  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      HandlerMethodValidationException.class,
      MethodArgumentTypeMismatchException.class,
      MissingServletRequestParameterException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErrorResponse> handleInvalidInput(Exception e) {
    log.debug("Invalid input: {}", e.getMessage());
    return toResponse(ErrorCode.INVALID_INPUT);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
    return toResponse(ErrorCode.NOT_FOUND);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException e) {
    return toResponse(ErrorCode.METHOD_NOT_ALLOWED);
  }

  /** 예상하지 못한 예외: 원인 파악을 위해 스택 트레이스를 남기고, 응답에는 일반 메시지만 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("Unexpected exception", e);
    return toResponse(ErrorCode.INTERNAL_ERROR);
  }

  private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
    return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode));
  }
}

package com.sprint.part3.sb01_monew_team6.exception.subscription;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.MonewException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * 구독 관련 예외의 부모 클래스
 */
public class SubscriptionException extends MonewException {
  public SubscriptionException(ErrorCode code, Instant timestamp, HttpStatus status) {
    super(code, timestamp, status);
  }
}

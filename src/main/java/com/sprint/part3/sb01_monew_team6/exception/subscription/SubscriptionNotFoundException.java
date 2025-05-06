package com.sprint.part3.sb01_monew_team6.exception.subscription;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * 구독 정보를 찾을 수 없을 때 발생하는 예외
 */
@Getter
public class SubscriptionNotFoundException extends SubscriptionException {

  private final Long userId;
  private final Long interestId;

  public SubscriptionNotFoundException(Long userId, Long interestId) {
    super(ErrorCode.SUBSCRIPTION_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND); // 404 Not Found
    this.userId = userId;
    this.interestId = interestId;
  }

  // ID 없이 ErrorCode만 받는 생성자 (필요하다면)
  public SubscriptionNotFoundException() {
    super(ErrorCode.SUBSCRIPTION_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
    this.userId = null;
    this.interestId = null;
  }
}

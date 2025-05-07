package com.sprint.part3.sb01_monew_team6.exception.subscription;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus; // HttpStatus 임포트

import java.time.Instant; // Instant 임포트

/**
 * 이미 구독 중인 관심사를 다시 구독하려고 할 때 발생하는 예외
 */
@Getter
public class SubscriptionAlreadyExistsException extends SubscriptionException { // <<<--- SubscriptionException 상속

  // userId, interestId 필드는 필요하다면 유지할 수 있습니다.
  private final Long userId;
  private final Long interestId;

  public SubscriptionAlreadyExistsException(Long userId, Long interestId) {
    // --- vvv 부모 생성자 호출 방식으로 변경 vvv ---
    super(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS, Instant.now(), HttpStatus.CONFLICT); // 409 Conflict
    // --- ^^^ 부모 생성자 호출 방식으로 변경 ^^^ ---
    this.userId = userId;
    this.interestId = interestId;
    // log.warn("User {} already subscribed to Interest {}", userId, interestId); // 필요시 로깅
  }

  // ErrorCode만 받는 생성자 (필요하다면)
  public SubscriptionAlreadyExistsException() {
    super(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS, Instant.now(), HttpStatus.CONFLICT);
    this.userId = null; // 또는 적절한 기본값
    this.interestId = null;
  }
}

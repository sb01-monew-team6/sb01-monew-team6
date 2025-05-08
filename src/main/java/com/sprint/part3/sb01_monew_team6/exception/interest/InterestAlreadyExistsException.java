package com.sprint.part3.sb01_monew_team6.exception.interest; // interest 예외 패키지

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import java.time.Instant;

// InterestException을 상속받도록 변경
public class InterestAlreadyExistsException extends InterestException {

  public InterestAlreadyExistsException(String name) {
    // 부모인 InterestException의 생성자를 호출합니다.
    // 이 예외 상황에 맞는 ErrorCode와 HttpStatus를 전달합니다.
    // ErrorCode Enum에 INTEREST_ALREADY_EXISTS 추가 필요.
    super(ErrorCode.INTEREST_ALREADY_EXISTS, Instant.now(), HttpStatus.CONFLICT); // 409 Conflict
    // log.warn("Interest name already exists: {}", name); // 필요시 로깅
  }
}
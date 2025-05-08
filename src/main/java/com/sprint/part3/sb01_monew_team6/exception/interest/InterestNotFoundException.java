package com.sprint.part3.sb01_monew_team6.exception.interest; // interest 예외 패키지

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import java.time.Instant;

// InterestException 상속 (Interest 관련 예외 그룹화)
public class InterestNotFoundException extends InterestException {

  public InterestNotFoundException(Long interestId) {
    // 부모 InterestException 생성자 호출
    // 관심사를 못 찾은 경우는 보통 404 Not Found 사용
    super(ErrorCode.INTEREST_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
    // log.warn("Interest not found with ID: {}", interestId); // 필요시 로깅
  }
}
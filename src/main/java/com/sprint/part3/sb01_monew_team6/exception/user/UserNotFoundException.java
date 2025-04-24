package com.sprint.part3.sb01_monew_team6.exception.user;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException; // UserException 임포트
import org.springframework.http.HttpStatus;
import java.time.Instant;

// UserException 상속 받도록 변경
public class UserNotFoundException extends UserException {
  public UserNotFoundException(Long userId) {
    // 부모 UserException 생성자 호출
    super(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
    // log.warn("User not found with ID: {}", userId); // 로깅 등에 활용 가능
  }

  public UserNotFoundException(String message) {
    // 메시지를 직접 설정하기보다 ErrorCode를 사용하는 것이 일관성에 좋음
    super(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
    // log.warn("User not found: {}", message); // 로깅 등에 활용 가능
  }
}
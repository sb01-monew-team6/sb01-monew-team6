package com.sprint.part3.sb01_monew_team6.exception.user; // 또는 exception.user 패키지

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException; // UserException 임포트
import org.springframework.http.HttpStatus;
import java.time.Instant;

// UserException 상속 받도록 변경
public class LoginFailedException extends UserException {
  public LoginFailedException() {
    // 부모 UserException 생성자 호출
    super(ErrorCode.LOGIN_FAILED, Instant.now(), HttpStatus.UNAUTHORIZED);
  }

  // 필요시 다른 생성자 추가 가능 (메시지 등)
  public LoginFailedException(String message) {
    // 메시지를 직접 설정하기보다 ErrorCode를 사용하는 것이 일관성에 좋음
    // 부모 생성자를 호출하며 ErrorCode를 전달하는 것이 기본
    super(ErrorCode.LOGIN_FAILED, Instant.now(), HttpStatus.UNAUTHORIZED);

  }
}
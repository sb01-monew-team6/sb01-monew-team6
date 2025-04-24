package com.sprint.part3.sb01_monew_team6.exception.user; // 또는 exception.user 패키지

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException; // UserException 임포트
import org.springframework.http.HttpStatus;
import java.time.Instant;

// UserException 상속 받도록 변경
public class EmailAlreadyExistsException extends UserException {

  public EmailAlreadyExistsException(String email) {
    // 부모 UserException 생성자 호출: 적절한 ErrorCode, 현재 시각, HttpStatus 전달
    super(ErrorCode.EMAIL_ALREADY_EXISTS, Instant.now(), HttpStatus.CONFLICT);

  }
}
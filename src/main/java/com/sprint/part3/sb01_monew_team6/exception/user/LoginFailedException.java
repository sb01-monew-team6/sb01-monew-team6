package com.sprint.part3.sb01_monew_team6.exception.user;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import org.springframework.http.HttpStatus;
import java.time.Instant;

public class LoginFailedException extends UserException {
  public LoginFailedException() {
    super(ErrorCode.LOGIN_FAILED, Instant.now(), HttpStatus.UNAUTHORIZED);
  }
  public LoginFailedException(String message) {
    super(ErrorCode.LOGIN_FAILED, Instant.now(), HttpStatus.UNAUTHORIZED);
  }
}
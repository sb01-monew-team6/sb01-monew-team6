package com.sprint.part3.sb01_monew_team6.exception.user;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import org.springframework.http.HttpStatus;
import java.time.Instant;

public class UserNotFoundException extends UserException {
  public UserNotFoundException(Long userId) {
    super(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
  }

  public UserNotFoundException(String message) {
    super(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
  }
}
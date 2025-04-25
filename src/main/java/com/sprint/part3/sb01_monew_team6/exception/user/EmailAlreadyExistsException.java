package com.sprint.part3.sb01_monew_team6.exception.user;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import org.springframework.http.HttpStatus;
import java.time.Instant;

public class EmailAlreadyExistsException extends UserException {
  public EmailAlreadyExistsException(String email) {
    super(ErrorCode.EMAIL_ALREADY_EXISTS, Instant.now(), HttpStatus.CONFLICT);
  }
}
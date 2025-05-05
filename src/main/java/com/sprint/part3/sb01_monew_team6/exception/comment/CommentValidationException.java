package com.sprint.part3.sb01_monew_team6.exception.comment;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import java.time.Instant;
import org.springframework.http.HttpStatus;

public class CommentValidationException extends CommentException {
  public CommentValidationException() {
    super(ErrorCode.VALIDATION_ERROR, Instant.now(), HttpStatus.BAD_REQUEST);
  }
}

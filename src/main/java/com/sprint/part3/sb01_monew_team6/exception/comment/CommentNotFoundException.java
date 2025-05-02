package com.sprint.part3.sb01_monew_team6.exception.comment;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import java.time.Instant;
import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends CommentException {
  public CommentNotFoundException() {
    super(ErrorCode.COMMENT_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
  }
}

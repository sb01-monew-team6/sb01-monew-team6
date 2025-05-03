package com.sprint.part3.sb01_monew_team6.exception.comment;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import java.time.Instant;
import org.springframework.http.HttpStatus;

public class CommentNotSoftDeletedException extends CommentException{

  public CommentNotSoftDeletedException() {
    super(ErrorCode.COMMENT_NOT_SOFT_DELETED, Instant.now(), HttpStatus.BAD_REQUEST);
  }
}

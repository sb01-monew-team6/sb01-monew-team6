package com.sprint.part3.sb01_monew_team6.exception.comment;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.MonewException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class CommentException extends MonewException {
    public CommentException(ErrorCode errorCode, Instant timestamp, HttpStatus status) {

        super(errorCode, timestamp, status);
    }
}

package com.sprint.part3.sb01_monew_team6.exception.user;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.MonewException;

public class UserException extends MonewException {
	public UserException(ErrorCode code, Instant timestamp, HttpStatus status) {
		super(code, timestamp, status);
	}
}

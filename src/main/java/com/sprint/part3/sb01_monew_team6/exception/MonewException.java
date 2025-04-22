package com.sprint.part3.sb01_monew_team6.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class MonewException extends RuntimeException {
	private final ErrorCode code;
	private final Instant timestamp;
	private final HttpStatus status;

	public MonewException(ErrorCode code, Instant timestamp, HttpStatus status) {
		super(code.getMessage());
		this.code = code;
		this.timestamp = timestamp;
		this.status = status;
	}
}

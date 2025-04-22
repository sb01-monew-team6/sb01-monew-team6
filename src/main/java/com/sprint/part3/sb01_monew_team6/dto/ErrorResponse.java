package com.sprint.part3.sb01_monew_team6.dto;

import java.time.Instant;

public record ErrorResponse(
	Instant timestamp,
	String code,
	String message,
	String exceptionType,
	int status
) {
}

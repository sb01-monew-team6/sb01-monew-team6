package com.sprint.part3.sb01_monew_team6.exception.user_activity;

import java.util.Map;

import lombok.Getter;

@Getter
public class UserActivityDomainException extends RuntimeException {
	private final Map<String, Object> details;

	public UserActivityDomainException(String message, Map<String, Object> details) {
		super(message);
		this.details = details;
	}
}

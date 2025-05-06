package com.sprint.part3.sb01_monew_team6.exception.notification;

import java.util.Map;

import lombok.Getter;

@Getter
public class NotificationDomainException extends RuntimeException {
	private final Map<String, Object> details;

	public NotificationDomainException(String message, Map<String, Object> details) {
		super(message);
		this.details = details;
	}
}

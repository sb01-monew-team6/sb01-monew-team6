package com.sprint.part3.sb01_monew_team6.controller;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationException;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	@GetMapping
	public String findAllByUserId(
		@RequestHeader("Monew-Request-User-Id") Long userId
	) {
		if (userId <= 0L) {
			throw new NotificationException(NOTIFICATION_INVALID_EXCEPTION, Instant.now(), BAD_REQUEST);
		}

		return "";
	}
}

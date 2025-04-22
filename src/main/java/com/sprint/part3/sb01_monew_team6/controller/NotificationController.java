package com.sprint.part3.sb01_monew_team6.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.part3.sb01_monew_team6.validation.group.NotificationValidationGroup;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated(NotificationValidationGroup.class)
public class NotificationController {

	@GetMapping
	public String findAllByUserId(
		@RequestHeader("Monew-Request-User-Id")
		@Min(value = 1, groups = NotificationValidationGroup.class) Long userId
	) {

		return "";
	}
}

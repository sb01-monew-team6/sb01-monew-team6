package com.sprint.part3.sb01_monew_team6.controller;

import static org.springframework.data.domain.Sort.Direction.*;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;
import com.sprint.part3.sb01_monew_team6.validation.group.NotificationValidationGroup;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated(NotificationValidationGroup.class)
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ResponseEntity<PageResponse<NotificationDto>> findAllByUserId(
		@RequestHeader("Monew-Request-User-Id") @Min(value = 1, groups = NotificationValidationGroup.class) Long userId,
		@RequestParam(required = false) Instant cursor,
		@PageableDefault(
			size = 50,
			sort = "createdAt",
			direction = DESC
		) Pageable pageable
	) {
		PageResponse<NotificationDto> notifications = notificationService.findAllByUserId(userId, cursor, pageable);

		return ResponseEntity.status(OK)
			.body(notifications);
	}

	@PatchMapping
	public ResponseEntity<Void> updateAll(
		@RequestHeader("Monew-Request-User-Id") @Min(value = 1, groups = NotificationValidationGroup.class) Long userId
	) {
		notificationService.updateAll(userId);

		return ResponseEntity.ok().build();
	}
}

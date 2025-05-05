package com.sprint.part3.sb01_monew_team6.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.UserActivityDto;
import com.sprint.part3.sb01_monew_team6.service.UserActivityService;
import com.sprint.part3.sb01_monew_team6.validation.group.UserActivityValidationGroup;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user-activities")
@Validated(UserActivityValidationGroup.class)
@RequiredArgsConstructor
public class UserActivityController {

	private final UserActivityService userActivityService;

	/**
	 * @param requestUserId 로그인용 id, 이후 서비스 로직에서 사용 안 함
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<UserActivityDto> findByUserId(
		@RequestHeader("Monew-Request-User-Id")
		@Min(value = 1, groups = UserActivityValidationGroup.class) Long requestUserId,

		@PathVariable
		@Min(value = 1, groups = UserActivityValidationGroup.class) Long userId
	) {

		UserActivityDto userActivity = userActivityService.findByUserId(userId);

		return ResponseEntity.ok()
			.body(userActivity);
	}
}

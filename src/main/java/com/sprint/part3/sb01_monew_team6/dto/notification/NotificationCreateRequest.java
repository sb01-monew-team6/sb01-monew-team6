package com.sprint.part3.sb01_monew_team6.dto.notification;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

public record NotificationCreateRequest(
	Long userId,
	Long resourceId,
	ResourceType resourceType,
	String resourceContent,
	Long articleCount
) {
}

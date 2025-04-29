package com.sprint.part3.sb01_monew_team6.event;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

public record NotificationCreateEvent(
	Long userId,
	Long resourceId,
	ResourceType resourceType,
	String resourceContent,
	Long articleCount
) {
}

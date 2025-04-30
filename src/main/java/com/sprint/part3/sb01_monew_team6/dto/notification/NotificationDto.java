package com.sprint.part3.sb01_monew_team6.dto.notification;

import java.time.Instant;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

public record NotificationDto(
	Long id,
	Instant createdAt,
	Instant updatedAt,
	boolean confirmed,
	Long userId,
	String content,
	ResourceType resourceType,
	Long resourceId
) {
}

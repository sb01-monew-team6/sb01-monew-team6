package com.sprint.part3.sb01_monew_team6.service;

import java.time.Instant;

import org.springframework.data.domain.Pageable;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationCreateRequest;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;

public interface NotificationService {

	PageResponse<NotificationDto> findAllByUserId(Long userId, Instant cursor, Instant after, Pageable pageable);

	void updateAllByUserId(Long userId);

	void updateByUserId(Long userId, Long notificationId);

	void deleteAllOlderThanWeek();

	void create(NotificationCreateEvent request);
}

package com.sprint.part3.sb01_monew_team6.service.impl;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {
	@Override
	public PageResponse<NotificationDto> findAllByUserId(Long userId, Instant createAt, Pageable pageable) {
		return null;
	}
}

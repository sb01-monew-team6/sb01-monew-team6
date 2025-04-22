package com.sprint.part3.sb01_monew_team6.service.impl;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.mapper.NotificationMapper;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.notification.NotificationRepository;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationMapper notificationMapper;
	private final PageResponseMapper pageResponseMapper;
	private final NotificationRepository notificationRepository;

	@Override
	public PageResponse<NotificationDto> findAllByUserId(Long userId, Instant createdAt, Pageable pageable) {

		Slice<NotificationDto> slice = notificationRepository.findAllByUserId(userId, createdAt, pageable)
			.map(notificationMapper::toDto);

		Instant nextCursor = null;
		if (!slice.getContent().isEmpty()) {
			nextCursor = slice.getContent().get(slice.getContent().size() - 1).createdAt();
		}

		Long totalElements = notificationRepository.count();

		return pageResponseMapper.fromSlice(
			slice,
			nextCursor,
			nextCursor,
			totalElements
		);
	}
}

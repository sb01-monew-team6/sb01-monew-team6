package com.sprint.part3.sb01_monew_team6.service.impl;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationException;
import com.sprint.part3.sb01_monew_team6.mapper.NotificationMapper;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.repository.notification.NotificationRepository;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationMapper notificationMapper;
	private final PageResponseMapper pageResponseMapper;
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;

	@Override
	public PageResponse<NotificationDto> findAllByUserId(Long userId, Instant createdAt, Pageable pageable) {

		if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
			throw new NotificationException(NOTIFICATION_USER_NOT_FOUND_EXCEPTION, Instant.now(), BAD_REQUEST);
		}

		Slice<NotificationDto> slice = notificationRepository.findAllByUserId(
				userId,
				Optional.ofNullable(createdAt).orElse(Instant.now()),
				pageable)
			.map(notificationMapper::toDto);

		Instant nextCursor = null;
		if (!slice.getContent().isEmpty()) {
			int lastIndex = slice.getContent().size() - 1;
			List<NotificationDto> content = slice.getContent();
			nextCursor = content.get(lastIndex).createdAt();
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

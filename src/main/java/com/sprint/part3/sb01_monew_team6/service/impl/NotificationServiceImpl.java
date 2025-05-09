package com.sprint.part3.sb01_monew_team6.service.impl;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;
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
	public PageResponse<NotificationDto> findAllByUserId(Long userId, Instant cursor, Instant after,
		Pageable pageable) {

		validateUserIdOrThrowApiException(userId);

		Slice<NotificationDto> slice = notificationRepository.findAllByUserId(
				userId,
				Optional.ofNullable(cursor).orElse(Instant.now()),
				Optional.ofNullable(after).orElse(Instant.now()),
				pageable)
			.map(notificationMapper::toDto);

		Instant nextCursor = getNextCursor(slice);

		Long totalElements = notificationRepository.countByUserIdAndConfirmedFalse(userId);

		return pageResponseMapper.fromSlice(
			slice,
			nextCursor,
			nextCursor,
			totalElements
		);
	}

	private static Instant getNextCursor(Slice<NotificationDto> slice) {
		Instant nextCursor = null;
		if (!slice.getContent().isEmpty()) {
			int lastIndex = slice.getContent().size() - 1;
			List<NotificationDto> content = slice.getContent();
			nextCursor = content.get(lastIndex).createdAt();
		}
		return nextCursor;
	}

	private void validateUserIdOrThrowApiException(Long userId) {
		if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
			throw new NotificationException(NOTIFICATION_USER_NOT_FOUND_EXCEPTION, Instant.now(), BAD_REQUEST);
		}
	}

	private void validateNotificationId(Long notificationId) {
		if (!notificationRepository.existsById(notificationId)) {
			throw new NotificationException(NOTIFICATION_USER_NOT_FOUND_EXCEPTION, Instant.now(), BAD_REQUEST);
		}
	}

	@Override
	@Transactional
	public void updateAllByUserId(Long userId) {

		validateUserIdOrThrowApiException(userId);

		notificationRepository.updateAllByUserId(userId);
	}

	@Override
	@Transactional
	public void updateByUserId(Long userId, Long notificationId) {

		validateUserIdOrThrowApiException(userId);
		validateNotificationId(notificationId);

		notificationRepository.updateByUserId(userId, notificationId);
	}

	@Override
	@Transactional
	public int deleteAllOlderThanWeek(Instant weekAgo, Limit limit) {

		return notificationRepository.deleteAllOlderThanWeek(weekAgo, limit);
	}

	@Override
	@Transactional
	public void createFromEvent(NotificationCreateEvent event) {

		User user = userRepository.findByIdAndIsDeletedFalse(event.userId())
			.orElseThrow(() -> new NotificationDomainException("유저를 찾을 수 없습니다.", Map.of("userId", event.userId())));

		String content = generateContent(event);

		Notification notification = Notification.createNotification(
			user,
			content,
			event.resourceType(),
			event.resourceId()
		);

		notificationRepository.save(notification);
	}

	private String generateContent(NotificationCreateEvent event) {
		return switch (event.resourceType()) {
			case INTEREST -> String.format(
				"[%s]와 관련된 기사가 %d건 등록되었습니다.",
				event.resourceContent(), event.articleCount()
			);
			case COMMENT -> String.format(
				"[%s]님이 나의 댓글을 좋아합니다.",
				userRepository.findByIdAndIsDeletedFalse(event.resourceId())
					.orElseThrow(
						() -> new NotificationDomainException("유저를 찾을 수 없습니다.", Map.of("resourceId", event.resourceId())))
					.getNickname()
			);
		};
	}
}

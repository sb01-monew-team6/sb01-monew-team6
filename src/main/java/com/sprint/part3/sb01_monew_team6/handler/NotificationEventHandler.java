package com.sprint.part3.sb01_monew_team6.handler;

import java.util.Map;
import java.util.Objects;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationService notificationService;

	@Async
	@TransactionalEventListener
	public void handleNotificationCreateEvent(NotificationCreateEvent event) {

		validateEvent(event);
		validateUserId(event.userId());
		validateByResourceType(event);

		notificationService.createFromEvent(event);
	}

	private static void validateByResourceType(NotificationCreateEvent event) {
		switch (event.resourceType()) {
			case INTEREST:
				validateResourceContent(event.resourceContent());
				validateArticleCount(event.articleCount());
				break;
		}
	}

	private static void validateArticleCount(Long articleCount) {
		if (Objects.isNull(articleCount) || articleCount <= 0) {
			throw new NotificationDomainException("기사의 개수가 유효하지 않습니다.",
				Map.of("articleCount", String.valueOf(articleCount)));
		}
	}

	private static void validateResourceContent(String resourceContent) {
		if (Objects.isNull(resourceContent) || resourceContent.isBlank()) {
			throw new NotificationDomainException("리소스 내용이 유효하지 않습니다.",
				Map.of("resourceContent", String.valueOf(resourceContent)));
		}
	}

	private static void validateUserId(Long userId) {
		if (Objects.isNull(userId) || userId <= 0) {
			throw new NotificationDomainException("유저 id 가 유효하지 않습니다.",
				Map.of("userId", String.valueOf(userId)));
		}
	}

	private static void validateEvent(NotificationCreateEvent event) {
		if (Objects.isNull(event)) {
			throw new UserActivityDomainException("이벤트가 null 일 수 없습니다.",
				Map.of("event", String.valueOf(event)));
		}
	}
}

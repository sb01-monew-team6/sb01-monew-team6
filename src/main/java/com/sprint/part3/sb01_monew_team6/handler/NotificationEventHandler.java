package com.sprint.part3.sb01_monew_team6.handler;

import java.util.Map;
import java.util.Objects;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;
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

		notificationService.createFromEvent(event);
	}

	private static void validateEvent(NotificationCreateEvent event) {

		if (Objects.isNull(event.userId()) || event.userId() <= 0) {
			throw new NotificationDomainException("유저 id 가 유효하지 않습니다.",
				Map.of("userId", String.valueOf(event.userId())));
		}

		switch (event.resourceType()) {
			case INTEREST:
				if (Objects.isNull(event.resourceContent()) || event.resourceContent().isBlank()) {
					throw new NotificationDomainException("리소스 내용이 유효하지 않습니다.",
						Map.of("resourceContent", event.resourceContent()));
				}

				if (Objects.isNull(event.articleCount()) || event.articleCount() <= 0) {
					throw new NotificationDomainException("기사의 개수가 유효하지 않습니다.",
						Map.of("articleCount", event.articleCount()));
				}
				break;
		}
	}
}

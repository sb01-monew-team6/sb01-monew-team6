package com.sprint.part3.sb01_monew_team6.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationService notificationService;

	@Async
	@TransactionalEventListener
	public void handle(NotificationCreateEvent event) {
		notificationService.createFromEvent(event);
	}
}

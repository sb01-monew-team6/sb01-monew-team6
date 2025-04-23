package com.sprint.part3.sb01_monew_team6.handler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private NotificationEventHandler notificationEventHandler;

	@Test
	@DisplayName("createNotificationByEventHandler 가 알림 생성 로직을 호출한다")
	public void createNotificationByEventHandler() throws Exception {
		//given
		doNothing().when(notificationService).create(any(NotificationCreateEvent.class));

		//when
		notificationEventHandler.handle();

		//then
		verify(notificationService, times(1)).create(any(NotificationCreateEvent.class));
	}
}
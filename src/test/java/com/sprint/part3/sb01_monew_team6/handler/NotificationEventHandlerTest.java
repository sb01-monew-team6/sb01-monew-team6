package com.sprint.part3.sb01_monew_team6.handler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
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
		NotificationCreateEvent event = new NotificationCreateEvent(
			1L,
			null,
			ResourceType.COMMENT,
			"유저",
			null
		);
		doNothing().when(notificationService).createFromEvent(event);

		//when
		notificationEventHandler.handle(event);

		//then
		verify(notificationService, times(1)).createFromEvent(event);
	}
}
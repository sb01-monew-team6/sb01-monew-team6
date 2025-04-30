package com.sprint.part3.sb01_monew_team6.scheduler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private NotificationScheduler notificationScheduler;

	@Test
	@DisplayName("deleteConfirmedNotificationsOlderThanWeek 가 알림 삭제 로직을 호출한다")
	public void deleteConfirmedNotificationsOlderThanWeekCallsDeleteAll() throws Exception {
		//given
		when(notificationService.deleteAllOlderThanWeek(any(), any()))
			.thenReturn(1)
			.thenReturn(0);

		//when
		notificationScheduler.deleteConfirmedNotificationsOlderThanWeek();

		//then
		verify(notificationService, times(2)).deleteAllOlderThanWeek(any(), any());
	}
}
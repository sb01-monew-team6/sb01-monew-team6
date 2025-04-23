package com.sprint.part3.sb01_monew_team6.scheduler;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sprint.part3.sb01_monew_team6.service.NotificationService;

class NotificationSchedulerTest {

	@MockitoBean
	private NotificationService notificationService;

	@Test
	@DisplayName("스케줄러에 의해 1주일이 지난 확인된 알림은 삭제된다")
	public void deleteConfirmedNotificationsOlderThanWeekDueToTheScheduler() throws Exception {
		//given
		Long userId = 1L;

		//when & then
		Awaitility.await()
			.atMost(3, TimeUnit.SECONDS)
			.untilAsserted(() ->
				verify(notificationService).deleteAllByUserId(userId)
			);
	}
}
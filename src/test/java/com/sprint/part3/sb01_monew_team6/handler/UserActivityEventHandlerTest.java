package com.sprint.part3.sb01_monew_team6.handler;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.event.UserActivityCreateEvent;
import com.sprint.part3.sb01_monew_team6.service.UserActivityService;

@ExtendWith(MockitoExtension.class)
class UserActivityEventHandlerTest {

	@Mock
	private UserActivityService userActivityService;

	@InjectMocks
	private UserActivityEventHandler userActivityEventHandler;

	@Test
	@DisplayName("addUserActivityByEventHandler 가 유저 활동 내역 추가 로직을 호출한다")
	public void addUserActivityByEventHandler() throws Exception {
		//given
		UserActivityCreateEvent event = new UserActivityCreateEvent(
			1L,
			new SubscriptionHistoryDto(
				1L,
				"name",
				List.of("k1", "k2"),
				3L
			),
			null,
			null,
			null
		);
		doNothing().when(userActivityService).addSubscriptionFromEvent(
			anyLong(), any(SubscriptionHistoryDto.class));

		//when
		userActivityEventHandler.handleUserActivityAddEvent(event);

		//then
		verify(userActivityService, times(1)).addSubscriptionFromEvent(
			anyLong(), any(SubscriptionHistoryDto.class));
	}

}
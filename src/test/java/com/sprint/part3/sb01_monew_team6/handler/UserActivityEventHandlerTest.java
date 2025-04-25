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
import com.sprint.part3.sb01_monew_team6.entity.enums.UserActivityType;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityRemoveEvent;
import com.sprint.part3.sb01_monew_team6.service.UserActivityService;
import com.sprint.part3.sb01_monew_team6.validation.UserActivityEventValidatorDispatcher;

@ExtendWith(MockitoExtension.class)
class UserActivityEventHandlerTest {

	@Mock
	private UserActivityService userActivityService;
	@Mock
	private UserActivityEventValidatorDispatcher validatorDispatcher;

	@InjectMocks
	private UserActivityEventHandler userActivityEventHandler;

	@Test
	@DisplayName("addUserActivityByEventHandler 가 유저 활동 내역 추가 로직을 호출한다")
	public void addUserActivityByEventHandler() throws Exception {
		//given
		UserActivityAddEvent event = new UserActivityAddEvent(
			1L,
			UserActivityType.SUBSCRIPTION,
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
		doNothing().when(validatorDispatcher).validate(event);
		doNothing().when(userActivityService).addSubscriptionFromEvent(
			anyLong(), any(SubscriptionHistoryDto.class));

		//when
		userActivityEventHandler.handleUserActivityAddEvent(event);

		//then
		verify(userActivityService, times(1)).addSubscriptionFromEvent(
			anyLong(), any(SubscriptionHistoryDto.class));
	}

	@Test
	@DisplayName("removeUserActivityByEventHandler 가 유저 활동 내역 제거 로직을 호출한다")
	public void removeUserActivityByEventHandler() throws Exception {
		//given
		UserActivityRemoveEvent event = new UserActivityRemoveEvent(
			1L,
			UserActivityType.SUBSCRIPTION,
			1L,
			null,
			null,
			null
		);
		doNothing().when(validatorDispatcher).validate(event);
		doNothing().when(userActivityService).removeSubscriptionFromEvent(
			anyLong(), any(SubscriptionHistoryDto.class));

		//when
		userActivityEventHandler.handleUserActivityRemoveEvent(event);

		//then
		verify(userActivityService, times(1)).removeSubscriptionFromEvent(
			anyLong(), any(SubscriptionHistoryDto.class));
	}

}
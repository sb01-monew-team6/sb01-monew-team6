package com.sprint.part3.sb01_monew_team6.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.repository.user_activity.UserActivityRepository;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

	@Mock
	private UserActivityRepository userActivityRepository;

	@InjectMocks
	private UserActivityService userActivityService;

	@Test
	@DisplayName("createFromEvent 정상 호출 시 정상적으로 레포지토리가 호출된다")
	public void createFromEvent() throws Exception {
		//given
		doNothing().when(userActivityRepository)
			.addSubscription(anyLong(), any(UserActivity.SubscriptionHistory.class));

		//when
		userActivityService.createFromEvent();

		//then
		verify(userActivityRepository, times(1)).addSubscription(anyLong(), any(UserActivity.SubscriptionHistory.class));

	}
}
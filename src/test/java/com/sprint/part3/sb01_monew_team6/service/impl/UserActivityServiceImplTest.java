package com.sprint.part3.sb01_monew_team6.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;
import com.sprint.part3.sb01_monew_team6.mapper.SubscriptionHistoryMapper;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.repository.user_activity.UserActivityRepository;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceImplTest {

	@Mock
	private SubscriptionHistoryMapper subscriptionHistoryMapper;
	@Mock
	private UserActivityRepository userActivityRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserActivityServiceImpl userActivityService;

	@Test
	@DisplayName("addSubscriptionFromEvent 정상 호출 시 정상적으로 레포지토리가 호출된다")
	public void addSubscriptionFromEvent() throws Exception {
		//given
		Long userId = 1L;
		UserActivity.SubscriptionHistory history = new UserActivity.SubscriptionHistory(
			1L,
			"interestName",
			List.of("k1", "k2"),
			3L
		);
		SubscriptionHistoryDto historyDto = new SubscriptionHistoryDto(
			1L,
			"interestName",
			List.of("k1", "k2"),
			3L
		);

		when(subscriptionHistoryMapper.fromDto(any(SubscriptionHistoryDto.class))).thenReturn(
			history);
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
		doNothing().when(userActivityRepository).addSubscription(anyLong(), any(UserActivity.SubscriptionHistory.class));

		//when
		userActivityService.addSubscriptionFromEvent(userId, historyDto);

		//then
		verify(userActivityRepository, times(1)).addSubscription(anyLong(),
			any(UserActivity.SubscriptionHistory.class));
	}

	@Test
	@DisplayName("addSubscriptionFromEvent 호출 시 user 가 존재하지 않는다면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenUserNonExistWhileAddSubscriptionFromEvent() throws Exception {
		//given
		Long userId = 1L;
		SubscriptionHistoryDto historyDto = new SubscriptionHistoryDto(
			1L,
			"interestName",
			List.of("k1", "k2"),
			3L
		);
		when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

		//when & then
		assertThatThrownBy(() ->
			userActivityService.addSubscriptionFromEvent(userId, historyDto)
		).isInstanceOf(UserActivityDomainException.class);

	}
}
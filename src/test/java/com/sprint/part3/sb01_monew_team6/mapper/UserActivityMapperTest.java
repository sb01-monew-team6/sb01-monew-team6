package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.UserActivityDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.UserActivityMapper;

class UserActivityMapperTest {

	private final UserActivityMapper userActivityMapper = Mappers.getMapper(UserActivityMapper.class);

	@Test
	@DisplayName("toDto 정상 호출 시 엔티티가 dto 로 정상 변환")
	public void returnDtoWhenToDtoSuccessfullyCalled() throws Exception {
		//given
		UserActivity userActivity = new UserActivity(
			1L,
			"email",
			"nickname",
			Instant.now(),
			List.of(new UserActivity.SubscriptionHistory(
				1L,
				"interestName",
				List.of("k"),
				3L,
				Instant.now()
			)),
			null,
			null,
			null
		);

		//when
		UserActivityDto dto = userActivityMapper.toDto(userActivity);

		//then
		assertThat(dto.id()).isEqualTo(1L);
		assertThat(dto.email()).isEqualTo("email");
		assertThat(dto.nickname()).isEqualTo("nickname");
		assertThat(dto.subscriptions()).hasSize(1);
		assertThat(dto.subscriptions().get(0).interestId()).isEqualTo(1L);
		assertThat(dto.subscriptions().get(0).interestName()).isEqualTo("interestName");
		assertThat(dto.subscriptions().get(0).interestKeywords()).containsExactly("k");
		assertThat(dto.subscriptions().get(0).interestSubscriberCount()).isEqualTo(3L);

	}

	@Test
	@DisplayName("fromUserDto 정상 호출 시 userDto 가 userActivity 로 정상 변환")
	public void returnUserActivityWhenFromUserDtoSuccessfullyCalled() throws Exception {
		//given
		UserDto userDto = new UserDto(
			1L,
			"asd@asd.asd",
			"asd",
			Instant.now()
		);

		//when
		UserActivity userActivity = userActivityMapper.fromUserDto(userDto);

		//then
		assertThat(userActivity.getUserId()).isEqualTo(userDto.id());
		assertThat(userActivity.getEmail()).isEqualTo(userDto.email());
		assertThat(userActivity.getNickname()).isEqualTo(userDto.nickname());
		assertThat(userActivity.getUserCreatedAt()).isEqualTo(userDto.createdAt());
	}
}
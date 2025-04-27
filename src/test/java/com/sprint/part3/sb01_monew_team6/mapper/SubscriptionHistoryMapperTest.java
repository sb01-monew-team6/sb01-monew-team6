package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.SubscriptionHistoryMapper;

class SubscriptionHistoryMapperTest {

	private final SubscriptionHistoryMapper mapper = Mappers.getMapper(SubscriptionHistoryMapper.class);

	@Test
	@DisplayName("fromDto 정상 호출 시 dto 가 엔티티 로 정상 변환")
	public void returnEntityWhenFromDtoSuccessfullyCalled() throws Exception {
		//given
		SubscriptionHistoryDto dto = new SubscriptionHistoryDto(
			1L,
			"name",
			List.of("k"),
			1L,
			Instant.now()
		);

		//when
		UserActivity.SubscriptionHistory entity = mapper.fromDto(dto);

		//then
		assertThat(entity.getInterestId()).isEqualTo(1L);
		assertThat(entity.getInterestName()).isEqualTo("name");
		assertThat(entity.getInterestKeywords()).containsExactly("k");
		assertThat(entity.getInterestSubscriberCount()).isEqualTo(1L);
	}
}
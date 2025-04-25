package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.CommentHistoryMapper;

class CommentHistoryMapperTest {
	
	private final CommentHistoryMapper mapper = Mappers.getMapper(CommentHistoryMapper.class);

	@Test
	@DisplayName("fromDto 정상 호출 시 dto 가 엔티티 로 정상 변환")
	public void returnEntityWhenFromDtoSuccessfullyCalled() throws Exception {
		//given
		CommentHistoryDto dto = new CommentHistoryDto(
			1L,
			"title",
			1L,
			"nickname",
			"content",
			1L
		);

		//when
		UserActivity.CommentHistory entity = mapper.fromDto(dto);

		//then
		assertThat(entity.getArticleId()).isEqualTo(1L);
		assertThat(entity.getArticleTitle()).isEqualTo("title");
		assertThat(entity.getUserId()).isEqualTo(1L);
		assertThat(entity.getUserNickname()).isEqualTo("nickname");
		assertThat(entity.getContent()).isEqualTo("content");
		assertThat(entity.getLikeCount()).isEqualTo(1L);
	}
}
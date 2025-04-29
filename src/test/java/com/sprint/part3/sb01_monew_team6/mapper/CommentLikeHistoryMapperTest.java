package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.CommentLikeHistoryMapper;

class CommentLikeHistoryMapperTest {

	private final CommentLikeHistoryMapper mapper = Mappers.getMapper(CommentLikeHistoryMapper.class);

	@Test
	@DisplayName("fromDto 정상 호출 시 dto 가 엔티티 로 정상 변환")
	public void returnEntityWhenFromDtoSuccessfullyCalled() throws Exception {
		//given
		CommentLikeHistoryDto dto = new CommentLikeHistoryDto(
			1L,
			1L,
			"title",
			1L,
			"nickname",
			"content",
			1L,
			Instant.now(),
			Instant.now()
		);

		//when
		UserActivity.CommentLikeHistory entity = mapper.fromDto(dto);

		//then
		assertThat(entity.getCommentId()).isEqualTo(1L);
		assertThat(entity.getArticleId()).isEqualTo(1L);
		assertThat(entity.getArticleTitle()).isEqualTo("title");
		assertThat(entity.getCommentUserId()).isEqualTo(1L);
		assertThat(entity.getCommentUserNickname()).isEqualTo("nickname");
		assertThat(entity.getCommentContent()).isEqualTo("content");
		assertThat(entity.getCommentLikeCount()).isEqualTo(1L);
	}
}
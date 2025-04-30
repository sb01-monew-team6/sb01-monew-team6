package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.ArticleViewHistoryMapper;

class ArticleViewHistoryMapperTest {

	private final ArticleViewHistoryMapper mapper = Mappers.getMapper(ArticleViewHistoryMapper.class);

	@Test
	@DisplayName("fromDto 정상 호출 시 dto 가 엔티티 로 정상 변환")
	public void returnEntityWhenFromDtoSuccessfullyCalled() throws Exception {
		//given
		ArticleViewHistoryDto dto = new ArticleViewHistoryDto(
			1L,
			1L,
			"src",
			"url",
			"title",
			LocalDateTime.now(),
			"summary",
			1L,
			1L,
			Instant.now()
		);

		//when
		UserActivity.ArticleViewHistory entity = mapper.fromDto(dto);

		//then
		assertThat(entity.getViewedBy()).isEqualTo(1L);
		assertThat(entity.getArticleId()).isEqualTo(1L);
		assertThat(entity.getSource()).isEqualTo("src");
		assertThat(entity.getSourceUrl()).isEqualTo("url");
		assertThat(entity.getArticleTitle()).isEqualTo("title");
		assertThat(entity.getArticleSummary()).isEqualTo("summary");
		assertThat(entity.getArticleCommentCount()).isEqualTo(1L);
		assertThat(entity.getArticleViewCount()).isEqualTo(1L);
	}
}
package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

class ArticleViewHistoryValidatorTest {

	private final ArticleViewHistoryValidator validator = new ArticleViewHistoryValidator();

	@Test
	@DisplayName("validateViewHistoryDto 호출 시 ArticleView 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenArticleViewIsNullWhileValidateViewHistoryDto() throws Exception {
		//given
		ArticleViewHistoryDto dto = null;

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateViewHistoryDto 호출 시 기사 id 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenArticleIdIsInvalidWhileValidateViewHistoryDto() throws Exception {
		//given
		ArticleViewHistoryDto dto = new ArticleViewHistoryDto(
			1L,
			0L,
			"src",
			"url",
			"title",
			LocalDateTime.now(),
			"summary",
			1L,
			1L,
			Instant.now()
		);

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateViewHistoryDto 호출 시 기사 조회 유저 id 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenViewedByIsInvalidWhileValidateViewHistoryDto() throws Exception {
		//given
		ArticleViewHistoryDto dto = new ArticleViewHistoryDto(
			0L,
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

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
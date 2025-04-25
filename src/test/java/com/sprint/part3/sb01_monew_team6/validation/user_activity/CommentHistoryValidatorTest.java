package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

class CommentHistoryValidatorTest {

	private final CommentHistoryValidator validator = new CommentHistoryValidator();

	@Test
	@DisplayName("validateCommentHistoryDto 호출 시 Comment 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenCommentIsNullWhileValidateCommentHistoryDto() throws
		Exception {
		//given
		CommentHistoryDto dto = null;

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateCommentHistoryDto 호출 시 기사 id 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenArticleIdIsInvalidWhileValidateCommentHistoryDto() throws
		Exception {
		//given
		CommentHistoryDto dto = new CommentHistoryDto(
			null,
			"title",
			1L,
			"nickName",
			"content",
			1L
		);

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
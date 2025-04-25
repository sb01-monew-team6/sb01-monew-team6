package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

class CommentHistoryValidatorTest {

	private final CommentLikeHistoryValidator validator = new CommentLikeHistoryValidator();

	@Test
	@DisplayName("validateCommentLikeHistoryDto 호출 시 CommentLike 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenCommentLikeIsNullWhileValidateCommentLikeHistoryDto() throws
		Exception {
		//given
		CommentLikeHistoryDto dto = null;

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
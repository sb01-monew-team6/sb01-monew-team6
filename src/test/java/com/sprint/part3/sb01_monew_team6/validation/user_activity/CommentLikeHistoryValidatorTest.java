package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

class CommentLikeHistoryValidatorTest {

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

	@Test
	@DisplayName("validateCommentLikeHistoryDto 호출 시 댓글 유저 id 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenCommentUserIdIsInvalidWhileValidateCommentLikeHistoryDto() throws
		Exception {
		//given
		CommentLikeHistoryDto dto = new CommentLikeHistoryDto(
			1L,
			1L,
			"title",
			0L,
			"nickName",
			"content",
			1L,
			Instant.now(),
			Instant.now()
		);

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateCommentLikeHistoryDto 호출 시 댓글 id 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenCommentIdIsInvalidWhileValidateCommentLikeHistoryDto() throws
		Exception {
		//given
		CommentLikeHistoryDto dto = new CommentLikeHistoryDto(
			0L,
			1L,
			"title",
			1L,
			"nickName",
			"content",
			1L,
			Instant.now(),
			Instant.now()
		);

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
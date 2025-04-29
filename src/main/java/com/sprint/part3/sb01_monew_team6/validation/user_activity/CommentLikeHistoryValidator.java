package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

@Component
public class CommentLikeHistoryValidator implements UserActivityValidator<CommentLikeHistoryDto> {

	@Override
	public void validate(CommentLikeHistoryDto commentLike) {
		if (Objects.isNull(commentLike)) {
			throw new UserActivityDomainException("댓글 좋아요 가 null 일 수 없습니다.",
				Map.of("commentLike", String.valueOf(commentLike)));
		}

		if (Objects.isNull(commentLike.commentUserId()) || commentLike.commentUserId() <= 0) {
			throw new UserActivityDomainException("댓글 유저 id 가 유효하지 않습니다.",
				Map.of("commentUserId", String.valueOf(commentLike.commentUserId())));
		}

		if (Objects.isNull(commentLike.commentId()) || commentLike.commentId() <= 0) {
			throw new UserActivityDomainException("댓글 id 가 유효하지 않습니다.",
				Map.of("commentId", String.valueOf(commentLike.commentId())));
		}
	}
}

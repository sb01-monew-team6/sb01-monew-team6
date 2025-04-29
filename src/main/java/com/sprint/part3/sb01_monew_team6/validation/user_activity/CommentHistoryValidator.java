package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

@Component
public class CommentHistoryValidator implements UserActivityValidator<CommentHistoryDto> {

	@Override
	public void validate(CommentHistoryDto comment) {
		if (Objects.isNull(comment)) {
			throw new UserActivityDomainException("댓글이 null 일 수 없습니다.",
				Map.of("comment", String.valueOf(comment)));
		}

		if (Objects.isNull(comment.articleId()) || comment.articleId() <= 0) {
			throw new UserActivityDomainException("기사 id 가 유효하지 않습니다.",
				Map.of("articleId", String.valueOf(comment.articleId())));
		}

		if (Objects.isNull(comment.likeCount()) || comment.likeCount() <= 0) {
			throw new UserActivityDomainException("댓글 좋아요 수 가 유효하지 않습니다.",
				Map.of("likeCount", String.valueOf(comment.likeCount())));
		}
	}
}

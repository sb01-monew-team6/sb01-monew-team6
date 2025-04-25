package com.sprint.part3.sb01_monew_team6.validation;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityRemoveEvent;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.ArticleViewHistoryValidator;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.CommentHistoryValidator;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.CommentLikeHistoryValidator;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.SubscriptionHistoryValidator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserActivityEventValidatorDispatcher {

	private final SubscriptionHistoryValidator subscriptionValidator;
	private final CommentLikeHistoryValidator commentLikeValidator;
	private final CommentHistoryValidator commentValidator;
	private final ArticleViewHistoryValidator articleViewValidator;

	public void validate(UserActivityAddEvent event) {

		validateEvent(event);
		validateUserId(event.userId());

		switch (event.type()) {
			case SUBSCRIPTION -> subscriptionValidator.validate(event.subscription());
			case COMMENT_LIKE -> commentLikeValidator.validate(event.commentLike());
			case COMMENT -> commentValidator.validate(event.comment());
			case ARTICLE_VIEW -> articleViewValidator.validate(event.articleView());
		}
	}

	private static void validateUserId(Long userId) {
		if (Objects.isNull(userId) || userId <= 0) {
			throw new UserActivityDomainException("유저 id 가 유효하지 않습니다.",
				Map.of("userId", String.valueOf(userId)));
		}
	}

	private static void validateEvent(Object event) {
		if (Objects.isNull(event)) {
			throw new UserActivityDomainException("이벤트가 null 일 수 없습니다.",
				Map.of("event", String.valueOf(event)));
		}
	}

	public void validate(UserActivityRemoveEvent event) {

		validateEvent(event);
		validateUserId(event.userId());

		switch (event.type()) {
			case SUBSCRIPTION -> validateInterestId(event.interestId());
			case COMMENT_LIKE -> validateCommentId(event.commentId());
		}
	}

	private static void validateInterestId(Long interestId) {
		if (Objects.isNull(interestId) || interestId <= 0) {
			throw new UserActivityDomainException("관심사 id 가 유효하지 않습니다.",
				Map.of("interestId", String.valueOf(interestId)));
		}
	}

	private static void validateCommentId(Long commentId) {
		if (Objects.isNull(commentId) || commentId <= 0) {
			throw new UserActivityDomainException("댓글 id 가 유효하지 않습니다.",
				Map.of("commentId", String.valueOf(commentId)));
		}
	}
}

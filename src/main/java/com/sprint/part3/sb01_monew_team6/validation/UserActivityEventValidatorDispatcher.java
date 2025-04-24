package com.sprint.part3.sb01_monew_team6.validation;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
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
		if (Objects.isNull(event.userId()) || event.userId() <= 0) {
			throw new UserActivityDomainException("유저 id 가 유효하지 않습니다.",
				Map.of("userId", String.valueOf(event.userId())));
		}

		switch (event.type()) {
			case SUBSCRIPTION -> subscriptionValidator.validate(event.subscription());
			case COMMENT_LIKE -> commentLikeValidator.validate(event.commentLike());
			case COMMENT -> commentValidator.validate(event.comment());
			case ARTICLE_VIEW -> articleViewValidator.validate(event.articleView());
		}
	}
}

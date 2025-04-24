package com.sprint.part3.sb01_monew_team6.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.service.UserActivityService;
import com.sprint.part3.sb01_monew_team6.validation.UserActivityEventValidatorDispatcher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserActivityEventHandler {

	private final UserActivityEventValidatorDispatcher validatorDispatcher;
	private final UserActivityService userActivityService;

	@Async
	@TransactionalEventListener
	public void handleUserActivityAddEvent(UserActivityAddEvent event) {

		validatorDispatcher.validate(event);

		switch (event.type()) {
			case SUBSCRIPTION -> userActivityService.addSubscriptionFromEvent(event.userId(), event.subscription());
			case COMMENT_LIKE -> userActivityService.addCommentLikeFromEvent(event.userId(), event.commentLike());
			case COMMENT -> userActivityService.addCommentFromEvent(event.userId(), event.comment());
			case ARTICLE_VIEW -> userActivityService.addArticleViewFromEvent(event.userId(), event.articleView());
		}
	}

}

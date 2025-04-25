package com.sprint.part3.sb01_monew_team6.validation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.ArticleViewHistoryValidator;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.CommentHistoryValidator;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.CommentLikeHistoryValidator;
import com.sprint.part3.sb01_monew_team6.validation.user_activity.SubscriptionHistoryValidator;

class UserActivityEventValidatorDispatcherTest {

	private final ArticleViewHistoryValidator articleViewHistoryValidator = new ArticleViewHistoryValidator();
	private final CommentHistoryValidator commentHistoryValidator = new CommentHistoryValidator();
	private final CommentLikeHistoryValidator commentLikeHistoryValidator = new CommentLikeHistoryValidator();
	private final SubscriptionHistoryValidator subscriptionHistoryValidator = new SubscriptionHistoryValidator();
	private final UserActivityEventValidatorDispatcher dispatcher = new UserActivityEventValidatorDispatcher(
		subscriptionHistoryValidator,
		commentLikeHistoryValidator,
		commentHistoryValidator,
		articleViewHistoryValidator
	);

	@Test
	@DisplayName("validateEvent 호출 시 event 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenEventIsNullWhileValidateEvent() throws
		Exception {
		//given
		UserActivityAddEvent event = null;

		//when & then
		assertThatThrownBy(() ->
			dispatcher.validate(event)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
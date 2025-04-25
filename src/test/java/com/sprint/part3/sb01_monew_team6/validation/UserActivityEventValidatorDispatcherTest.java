package com.sprint.part3.sb01_monew_team6.validation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.enums.UserActivityType;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityRemoveEvent;
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
	@DisplayName("validateEvent 호출 시 addEvent 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenAddEventIsNullWhileValidateEvent() throws
		Exception {
		//given
		UserActivityAddEvent event = null;

		//when & then
		assertThatThrownBy(() ->
			dispatcher.validate(event)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateAddEvent 호출 시 userId 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenUserIdIsInvalidWhileValidateAddEvent() throws
		Exception {
		//given
		UserActivityAddEvent event = new UserActivityAddEvent(
			null,
			UserActivityType.SUBSCRIPTION,
			new SubscriptionHistoryDto(
				1L,
				"name",
				List.of("k"),
				1L
			),
			null,
			null,
			null
		);

		//when & then
		assertThatThrownBy(() ->
			dispatcher.validate(event)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateEvent 호출 시 removeEvent 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenRemoveEventIsNullWhileValidateEvent() throws
		Exception {
		//given
		UserActivityRemoveEvent event = null;

		//when & then
		assertThatThrownBy(() ->
			dispatcher.validate(event)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateRemoveEvent 호출 시 userId 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenUserIdIsInvalidWhileValidateRemoveEvent() throws
		Exception {
		//given
		UserActivityRemoveEvent event = new UserActivityRemoveEvent(
			null,
			UserActivityType.SUBSCRIPTION,
			1L,
			null,
			null,
			null
		);

		//when & then
		assertThatThrownBy(() ->
			dispatcher.validate(event)
		).isInstanceOf(UserActivityDomainException.class);
	}

	@Test
	@DisplayName("validateRemoveEvent 호출 시 type 이 구독인 경우 interestId 가 유효하지 않으면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenInterestIdIsInvalidWhileValidateRemoveEvent() throws
		Exception {
		//given
		UserActivityRemoveEvent event = new UserActivityRemoveEvent(
			1L,
			UserActivityType.SUBSCRIPTION,
			0L,
			null,
			null,
			null
		);

		//when & then
		assertThatThrownBy(() ->
			dispatcher.validate(event)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
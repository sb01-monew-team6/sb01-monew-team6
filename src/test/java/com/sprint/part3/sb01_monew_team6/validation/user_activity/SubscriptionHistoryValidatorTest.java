package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

class SubscriptionHistoryValidatorTest {

	private final SubscriptionHistoryValidator validator = new SubscriptionHistoryValidator();

	@Test
	@DisplayName("validateSubscriptionHistoryDto 호출 시 Subscription 이 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenSubscriptionIsNullWhileValidateSubscriptionHistoryDto() throws
		Exception {
		//given
		SubscriptionHistoryDto dto = null;

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
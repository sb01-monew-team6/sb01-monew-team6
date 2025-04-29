package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

@Component
public class SubscriptionHistoryValidator implements UserActivityValidator<SubscriptionHistoryDto> {

	@Override
	public void validate(SubscriptionHistoryDto subscription) {
		if (Objects.isNull(subscription)) {
			throw new UserActivityDomainException("구독이 null 일 수 없습니다.",
				Map.of("subscription", String.valueOf(subscription)));
		}

		if (Objects.isNull(subscription.interestId()) || subscription.interestId() <= 0) {
			throw new UserActivityDomainException("관심사 id 가 유효하지 않습니다.",
				Map.of("interestId", String.valueOf(subscription.interestId())));
		}

		if (Objects.isNull(subscription.interestName()) || subscription.interestName().isBlank()) {
			throw new UserActivityDomainException("관심사 이름이 유효하지 않습니다.",
				Map.of("interestName", String.valueOf(subscription.interestName())));
		}
	}
}

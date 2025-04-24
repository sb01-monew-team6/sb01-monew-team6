package com.sprint.part3.sb01_monew_team6.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserActivityTest {

	@Test
	@DisplayName("빌더 패턴으로 유저 활동 내역이 정상적으로 생성된다")
	public void createUserActivityByBuilder() throws Exception {
		//given
		Instant now = Instant.now();
		UserActivity.SubscriptionHistory subscription = UserActivity.SubscriptionHistory.builder()
			.id(1L)
			.interestId(10L)
			.interestName("AI")
			.interestKeywords(List.of("ChatGPT", "머신러닝"))
			.interestSubscriberCount(10L)
			.createdAt(now)
			.build();

		//when
		UserActivity userActivity = UserActivity.builder()
			.email("email@google.com")
			.nickName("구글러")
			.subscriptions(List.of(subscription))
			.build();

		//then
		assertThat(userActivity.getEmail()).isEqualTo("email@google.com");
		assertThat(userActivity.getNickName()).isEqualTo("구글러");
		assertThat(userActivity.getSubscriptions()).hasSize(1);
		assertThat(userActivity.getSubscriptions().get(0).getInterestName()).isEqualTo("AI");
		assertThat(userActivity.getSubscriptions().get(0).getInterestKeywords()).containsExactly("ChatGPT", "머신러닝");
		assertThat(userActivity.getSubscriptions().get(0).getCreatedAt()).isEqualTo(now);
	}
}
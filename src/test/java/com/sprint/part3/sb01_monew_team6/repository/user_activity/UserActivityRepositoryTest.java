package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.sprint.part3.sb01_monew_team6.config.TestDataMongoConfig;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;
import com.sprint.part3.sb01_monew_team6.repository.UserActivityRepository;

@DataMongoTest
@ActiveProfiles("test")
@Import(TestDataMongoConfig.class)
class UserActivityRepositoryTest {

	@Autowired
	private UserActivityRepository userActivityRepository;

	@Test
	@DisplayName("몽고 db 에 userActivity 를 저장하고 정상적으로 찾는다")
	public void saveAndFindSuccessfully() throws Exception {
	    //given
		UserActivity.SubscriptionHistory subscription = UserActivity.SubscriptionHistory.builder()
			.interestId(10L)
			.interestName("AI")
			.interestKeywords(List.of("ChatGPT", "머신러닝"))
			.interestSubscriberCount(10L)
			.build();

		UserActivity userActivity = UserActivity.builder()
			.email("email@google.com")
			.nickName("구글러")
			.subscriptions(List.of(subscription))
			.build();

	    //when
		userActivityRepository.save(userActivity);
		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(userActivity.getId());
		assertThat(found.get().getEmail()).isEqualTo("email@google.com");
		assertThat(found.get().getNickName()).isEqualTo("구글러");
		assertThat(found.get().getSubscriptions()).hasSize(1);
		assertThat(found.get().getSubscriptions().get(0).getInterestName()).isEqualTo("AI");
		assertThat(found.get().getSubscriptions().get(0).getInterestKeywords()).containsExactly("ChatGPT", "머신러닝");
		assertThat(found.get().getSubscriptions().get(0).getId()).isEqualTo(subscription.getId());
	}

	@Test
	@DisplayName("addSubscription 정상 호출 시 정상적으로 몽고 db 에 적재된다")
	public void addSubscriptionSuccessfully() throws Exception {
	    //given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickName("구글러")
			.build();

		userActivityRepository.save(userActivity);

	    //when
		for (int i = 0; i < 12; ++i) {
			UserActivity.SubscriptionHistory subscription = UserActivity.SubscriptionHistory.builder()
				.interestId(10L + i)
				.interestName("AI" + i)
				.interestKeywords(List.of("ChatGPT" + i, "머신러닝" + i))
				.interestSubscriberCount(10L + i)
				.build();

			userActivityRepository.addSubscription(userId, subscription);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(userActivity.getId());
		assertThat(found.get().getUserId()).isEqualTo(userId);
		assertThat(found.get().getEmail()).isEqualTo("email@google.com");
		assertThat(found.get().getNickName()).isEqualTo("구글러");
		assertThat(found.get().getSubscriptions()).hasSize(10);
		assertThat(found.get().getSubscriptions().get(0).getInterestName()).isEqualTo("AI2");
		assertThat(found.get().getSubscriptions().get(0).getInterestName()).isEqualTo("AI11");
	}
}
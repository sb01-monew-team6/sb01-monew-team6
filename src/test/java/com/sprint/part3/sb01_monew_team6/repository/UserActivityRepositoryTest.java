package com.sprint.part3.sb01_monew_team6.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
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
	}
}
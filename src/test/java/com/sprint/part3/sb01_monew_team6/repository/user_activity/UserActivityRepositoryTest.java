package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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

	@AfterEach
	void cleanUp() {
		userActivityRepository.deleteAll();
	}

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
			.nickname("구글러")
			.subscriptions(List.of(subscription))
			.build();

		//when
		userActivityRepository.save(userActivity);
		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(userActivity.getId());
		assertThat(found.get().getEmail()).isEqualTo("email@google.com");
		assertThat(found.get().getNickname()).isEqualTo("구글러");
		assertThat(found.get().getSubscriptions()).hasSize(1);
		assertThat(found.get().getSubscriptions().get(0).getInterestName()).isEqualTo("AI");
		assertThat(found.get().getSubscriptions().get(0).getInterestKeywords()).containsExactly("ChatGPT", "머신러닝");
		assertThat(found.get().getSubscriptions().get(0).getInterestId()).isEqualTo(subscription.getInterestId());
	}

	@Test
	@DisplayName("addSubscription 정상 호출 시 정상적으로 몽고 db 에 적재된다")
	public void addSubscriptionSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
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
		assertThat(found.get().getNickname()).isEqualTo("구글러");
		assertThat(found.get().getSubscriptions().get(0).getInterestName()).isEqualTo("AI0");
		assertThat(found.get().getSubscriptions().get(11).getInterestName()).isEqualTo("AI11");
	}

	@Test
	@DisplayName("removeSubscription 정상 호출 시 정상적으로 몽고 db 에서 삭제된다")
	public void removeSubscriptionSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		for (int i = 0; i < 12; ++i) {
			UserActivity.SubscriptionHistory subscription = UserActivity.SubscriptionHistory.builder()
				.interestId(10L + i)
				.interestName("AI" + i)
				.interestKeywords(List.of("ChatGPT" + i, "머신러닝" + i))
				.interestSubscriberCount(10L + i)
				.build();

			userActivityRepository.addSubscription(userId, subscription);
		}

		//when
		for (int i = 0; i < 12; ++i) {
			userActivityRepository.removeSubscription(userId, 10L + i);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getSubscriptions()).isEmpty();
	}

	@Test
	@DisplayName("addCommentLike 정상 호출 시 정상적으로 몽고 db 에 적재된다")
	public void addCommentLikeSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		//when
		for (int i = 0; i < 12; ++i) {
			UserActivity.CommentLikeHistory commentLike = UserActivity.CommentLikeHistory.builder()
				.commentId(1L + i)
				.articleId(10L + i)
				.articleTitle("title" + i)
				.commentUserId(100L + i)
				.commentUserNickname("nickname" + i)
				.commentContent("hello" + i)
				.commentLikeCount(1000L + i)
				.createdAt(Instant.now())
				.build();

			userActivityRepository.addCommentLike(userId, commentLike);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(userActivity.getId());
		assertThat(found.get().getEmail()).isEqualTo("email@google.com");
		assertThat(found.get().getNickname()).isEqualTo("구글러");
		assertThat(found.get().getCommentLikes()).hasSize(10);
		assertThat(found.get().getCommentLikes().get(0).getCommentId()).isEqualTo(3);
		assertThat(found.get().getCommentLikes().get(9).getCommentId()).isEqualTo(12);
	}

	@Test
	@DisplayName("removeCommentLike 정상 호출 시 정상적으로 몽고 db 에서 삭제된다")
	public void removeCommentLikeSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		//when
		for (int i = 0; i < 12; ++i) {
			UserActivity.CommentLikeHistory commentLike = UserActivity.CommentLikeHistory.builder()
				.commentId(1L + i)
				.articleId(10L + i)
				.articleTitle("title" + i)
				.commentUserId(100L + i)
				.commentUserNickname("nickname" + i)
				.commentContent("hello" + i)
				.commentLikeCount(1000L + i)
				.build();

			userActivityRepository.addCommentLike(userId, commentLike);
		}

		//when
		for (int i = 0; i < 12; ++i) {
			userActivityRepository.removeCommentLike(userId, 1L + i);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getCommentLikes()).isEmpty();
	}

	@Test
	@DisplayName("addComment 정상 호출 시 정상적으로 몽고 db 에 적재된다")
	public void addCommentSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		//when
		for (int i = 0; i < 12; ++i) {
			UserActivity.CommentHistory comment = UserActivity.CommentHistory.builder()
				.articleId(1L + i)
				.userId(10L + i)
				.articleTitle("title" + i)
				.userNickname("nickname" + i)
				.content("content" + i)
				.likeCount(100L + i)
				.createdAt(Instant.now())
				.build();

			userActivityRepository.addComment(userId, comment);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(userActivity.getId());
		assertThat(found.get().getEmail()).isEqualTo("email@google.com");
		assertThat(found.get().getNickname()).isEqualTo("구글러");
		assertThat(found.get().getComments()).hasSize(10);
		assertThat(found.get().getComments().get(0).getArticleId()).isEqualTo(3);
		assertThat(found.get().getComments().get(9).getArticleId()).isEqualTo(12);
	}

	@Test
	@DisplayName("removeComment 정상 호출 시 정상적으로 몽고 db 에서 삭제된다")
	public void removeCommentSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		//when
		for (int i = 0; i < 12; ++i) {
			UserActivity.CommentHistory comment = UserActivity.CommentHistory.builder()
				.articleId(1L + i)
				.userId(10L + i)
				.articleTitle("title" + i)
				.userNickname("nickname" + i)
				.content("content" + i)
				.likeCount(100L + i)
				.build();

			userActivityRepository.addComment(userId, comment);
		}

		//when
		for (int i = 0; i < 12; ++i) {
			userActivityRepository.removeComment(userId, 1L + i);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getComments()).isEmpty();
	}

	@Test
	@DisplayName("addArticleView 정상 호출 시 정상적으로 몽고 db 에 적재된다")
	public void addArticleViewSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		//when
		for (int i = 0; i < 12; ++i) {
			UserActivity.ArticleViewHistory articleView = UserActivity.ArticleViewHistory.builder()
				.viewedBy(1L + i)
				.createdAt(Instant.now())
				.build();

			userActivityRepository.addArticleView(userId, articleView);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(userActivity.getId());
		assertThat(found.get().getUserId()).isEqualTo(userId);
		assertThat(found.get().getEmail()).isEqualTo("email@google.com");
		assertThat(found.get().getNickname()).isEqualTo("구글러");
		assertThat(found.get().getArticleViews()).hasSize(10);
		assertThat(found.get().getArticleViews().get(0).getViewedBy()).isEqualTo(3);
		assertThat(found.get().getArticleViews().get(9).getViewedBy()).isEqualTo(12);
	}

	@Test
	@DisplayName("removeArticleView 정상 호출 시 정상적으로 몽고 db 에서 삭제된다")
	public void removeArticleViewSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		UserActivity userActivity = UserActivity.builder()
			.userId(userId)
			.email("email@google.com")
			.nickname("구글러")
			.build();

		userActivityRepository.save(userActivity);

		for (int i = 0; i < 12; ++i) {
			UserActivity.ArticleViewHistory articleView = UserActivity.ArticleViewHistory.builder()
				.viewedBy(1L + i)
				.build();

			userActivityRepository.addArticleView(userId, articleView);
		}

		//when
		for (int i = 0; i < 12; ++i) {
			userActivityRepository.removeArticleView(userId, 1L + i);
		}

		Optional<UserActivity> found = userActivityRepository.findById(userActivity.getId());

		//then
		assertThat(found).isPresent();
		assertThat(found.get().getArticleViews()).isEmpty();
	}

	// @Test
	// @DisplayName("findByUserId 정상 호출 시 정상적으로 값이 조회된다")
	// public void findByUserIdSuccessfully() throws Exception {
	// 	//given
	// 	Long userId = 1L;
	//
	// 	UserActivity userActivity = UserActivity.builder()
	// 		.userId(userId)
	// 		.email("email@google.com")
	// 		.nickname("구글러")
	// 		.build();
	//
	// 	userActivityRepository.save(userActivity);
	//
	// 	for (int i = 0; i < 12; ++i) {
	// 		UserActivity.CommentHistory comment = UserActivity.CommentHistory.builder()
	// 			.articleId(1L + i)
	// 			.userId(10L + i)
	// 			.articleTitle("title" + i)
	// 			.userNickname("nickname" + i)
	// 			.content("content" + i)
	// 			.likeCount(100L + i)
	// 			.build();
	// 		UserActivity.ArticleViewHistory articleView = UserActivity.ArticleViewHistory.builder()
	// 			.viewedBy(1L + i)
	// 			.build();
	// 		UserActivity.CommentLikeHistory commentLike = UserActivity.CommentLikeHistory.builder()
	// 			.commentId(1L + i)
	// 			.articleId(10L + i)
	// 			.articleTitle("title" + i)
	// 			.commentUserId(100L + i)
	// 			.commentUserNickname("nickname" + i)
	// 			.commentContent("hello" + i)
	// 			.commentLikeCount(1000L + i)
	// 			.build();
	// 		UserActivity.SubscriptionHistory subscription = UserActivity.SubscriptionHistory.builder()
	// 			.interestId(10L + i)
	// 			.interestName("AI" + i)
	// 			.interestKeywords(List.of("ChatGPT" + i, "머신러닝" + i))
	// 			.interestSubscriberCount(10L + i)
	// 			.build();
	//
	// 		userActivityRepository.addSubscription(userId, subscription);
	// 		userActivityRepository.addCommentLike(userId, commentLike);
	// 		userActivityRepository.addArticleView(userId, articleView);
	// 		userActivityRepository.addComment(userId, comment);
	// 	}
	//
	// 	//when
	// 	Optional<UserActivity> found = userActivityRepository.findByUserId(userId);
	//
	// 	//then
	// 	assertThat(found).isPresent();
	// 	assertThat(found.get().getId()).isEqualTo(userActivity.getId());
	// 	assertThat(found.get().getUserId()).isEqualTo(userId);
	// 	assertThat(found.get().getEmail()).isEqualTo("email@google.com");
	// 	assertThat(found.get().getNickname()).isEqualTo("구글러");
	// 	assertThat(found.get().getSubscriptions().get(0).getInterestName()).isEqualTo("AI0");
	// 	assertThat(found.get().getSubscriptions().get(9).getInterestName()).isEqualTo("AI9");
	// 	assertThat(found.get().getCommentLikes()).hasSize(10);
	// 	assertThat(found.get().getCommentLikes().get(0).getCommentId()).isEqualTo(1);
	// 	assertThat(found.get().getCommentLikes().get(9).getCommentId()).isEqualTo(10);
	// 	assertThat(found.get().getComments()).hasSize(10);
	// 	assertThat(found.get().getComments().get(0).getArticleId()).isEqualTo(1);
	// 	assertThat(found.get().getComments().get(9).getArticleId()).isEqualTo(10);
	// 	assertThat(found.get().getArticleViews()).hasSize(10);
	// 	assertThat(found.get().getArticleViews().get(0).getViewedBy()).isEqualTo(1);
	// 	assertThat(found.get().getArticleViews().get(9).getViewedBy()).isEqualTo(10);
	// }

}
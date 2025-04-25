package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserActivityRepositoryImpl implements UserActivityRepositoryCustom {

	private final MongoTemplate mongoTemplate;

	@Override
	public void addSubscription(Long userId, UserActivity.SubscriptionHistory subscription) {
		Query query = queryByUserId(userId);
		Update update = new Update().push("subscriptions", subscription);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeSubscription(Long userId, Long interestId) {
		Query query = queryByUserId(userId);
		Update update = new Update().pull("subscriptions",
			Query.query(Criteria.where("interestId").is(interestId)).getQueryObject()
		);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void addCommentLike(Long userId, UserActivity.CommentLikeHistory commentLike) {
		Query query = queryByUserId(userId);
		Update update = new Update().push("commentLikes", commentLike);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeCommentLike(Long userId, Long commentId) {
		Query query = queryByUserId(userId);
		Update update = new Update().pull("commentLikes",
			Query.query(Criteria.where("commentId").is(commentId)).getQueryObject()
		);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void addComment(Long userId, UserActivity.CommentHistory comment) {
		Query query = queryByUserId(userId);
		Update update = new Update().push("comments", comment);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeComment(Long userId, Long articleId) {
		Query query = queryByUserId(userId);
		Update update = new Update().pull("comments",
			Query.query(Criteria.where("articleId").is(articleId)).getQueryObject()
		);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void addArticleView(Long userId, UserActivity.ArticleViewHistory articleView) {
		Query query = queryByUserId(userId);
		Update update = new Update().push("articleViews", articleView);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeArticleView(Long userId, Long viewedBy) {
		Query query = queryByUserId(userId);
		Update update = new Update().pull("articleViews",
			Query.query(Criteria.where("viewedBy").is(viewedBy)).getQueryObject()
		);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	private static Query queryByUserId(Long userId) {
		return new Query(Criteria.where("userId").is(userId));
	}

	@Override
	public Optional<UserActivity> findByUserId(Long userId) {
		Aggregation aggregation = Aggregation.newAggregation(
			match(Criteria.where("userId").is(userId)),

			project()
				.and("comments").slice(10).as("comments")
				.and("commentLikes").slice(10).as("commentLikes")
				.and("articleViews").slice(10).as("articleViews")
				.and("subscriptions").as("subscriptions")
				.and("userId").as("userId")
				.and("email").as("email")
				.and("nickname").as("nickname")
				.and("userCreatedAt").as("userCreatedAt")
				.and("createdAt").as("createdAt"),

			unwind("articleViews"),
			sort(Sort.by(Sort.Order.desc("articleViews.articlePublishedDate"))),

			group("_id")
				.first("userId").as("userId")
				.first("email").as("email")
				.first("nickname").as("nickname")
				.first("userCreatedAt").as("userCreatedAt")
				.first("createdAt").as("createdAt")
				.first("comments").as("comments")
				.first("subscriptions").as("subscriptions")
				.first("commentLikes").as("commentLikes")
				.push("articleViews").as("articleViews"),

			project()
				.and("userId").as("userId")
				.and("email").as("email")
				.and("nickname").as("nickname")
				.and("userCreatedAt").as("userCreatedAt")
				.and("createdAt").as("createdAt")
				.and("comments").as("comments")
				.and("commentLikes").as("commentLikes")
				.and("articleViews").as("articleViews")
				.and("subscriptions").as("subscriptions")
		);

		AggregationResults<UserActivity> result = mongoTemplate.aggregate(aggregation, UserActivity.class, UserActivity.class);
		UserActivity userActivity = result.getUniqueMappedResult();
		return Optional.ofNullable(userActivity);
	}
}

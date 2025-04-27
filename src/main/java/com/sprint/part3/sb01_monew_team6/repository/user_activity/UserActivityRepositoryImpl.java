package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.util.Collections;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
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

		BasicDBObject push = new BasicDBObject();
		push.put("$each", Collections.singletonList(commentLike));
		push.put("$slice", -10);

		Update update = new Update().push("commentLikes", push);
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

		BasicDBObject push = new BasicDBObject();
		push.put("$each", Collections.singletonList(comment));
		push.put("$slice", -10);

		Update update = new Update().push("comments", push);
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

		BasicDBObject push = new BasicDBObject();
		push.put("$each", Collections.singletonList(articleView));
		push.put("$slice", -10);

		Update update = new Update().push("articleViews", push);
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

		MatchOperation matchUser = Aggregation.match(Criteria.where("userId").is(userId));

		UnwindOperation unwindComments = unwind("comments");
		SortOperation sortComments = sort(Sort.by(Sort.Order.desc("comments.createdAt")));
		// LimitOperation limitComments = limit(10);
		GroupOperation groupComments = group("_id")
			.push("comments").as("comments");

		UnwindOperation unwindLikes = unwind("commentLikes");
		SortOperation sortLikes = sort(Sort.by(Sort.Order.desc("commentLikes.createdAt")));
		// LimitOperation limitLikes = limit(10);
		GroupOperation groupLikes = group("_id")
			.push("commentLikes").as("commentLikes");

		UnwindOperation unwindViews = unwind("articleViews");
		SortOperation sortViews = sort(Sort.by(Sort.Order.desc("articleViews.createdAt")));
		// LimitOperation limitViews = limit(10);
		GroupOperation groupViews = group("_id")
			.push("articleViews").as("articleViews");

		GroupOperation groupProfile = group("_id")
			.first("$$ROOT").as("profile");

		FacetOperation facet =
			facet(unwindComments, sortComments, groupComments)
				.as("commentsFacet")
				.and(unwindLikes, sortLikes, groupLikes)
				.as("likesFacet")
				.and(unwindViews, sortViews, groupViews)
				.as("viewsFacet")
				.and(groupProfile)
				.as("profileFacet");

		UnwindOperation unwindBase = unwind("profileFacet");

		ProjectionOperation project = Aggregation.project()
			.and("profileFacet.profile.userId").as("userId")
			.and("profileFacet.profile.email").as("email")
			.and("profileFacet.profile.nickname").as("nickname")
			.and("profileFacet.profile.userCreatedAt").as("userCreatedAt")
			.and("profileFacet.profile.subscriptions").as("subscriptions")
			.and("commentsFacet.comments").arrayElementAt(0).as("comments")
			.and("likesFacet.commentLikes").arrayElementAt(0).as("commentLikes")
			.and("viewsFacet.articleViews").arrayElementAt(0).as("articleViews");

		Aggregation aggregation = newAggregation(
			matchUser,
			facet,
			unwindBase,
			project
		);

		UserActivity userActivity = mongoTemplate
			.aggregate(aggregation, "user_activities", UserActivity.class)
			.getUniqueMappedResult();

		return Optional.ofNullable(userActivity);
	}

}

package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
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
		Query query = new Query(Criteria.where("userId").is(userId));
		Update update = new Update().push("subscriptions", subscription);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeSubscription(Long userId, Long interestId) {
		Query query = new Query(Criteria.where("userId").is(userId));
		Update update = new Update().pull("subscriptions",
			Query.query(Criteria.where("interestId").is(interestId)).getQueryObject()
		);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}
}

package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

public interface UserActivityRepositoryCustom {

	void addSubscription(Long userId, UserActivity.SubscriptionHistory subscription);

	void removeSubscription(Long userId, Long interestId);
}

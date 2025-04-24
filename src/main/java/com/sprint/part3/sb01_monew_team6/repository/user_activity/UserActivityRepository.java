package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

public interface UserActivityRepository extends MongoRepository<UserActivity, String>, UserActivityRepositoryCustom {
}

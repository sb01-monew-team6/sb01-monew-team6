package com.sprint.part3.sb01_monew_team6.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;
import com.sprint.part3.sb01_monew_team6.mapper.SubscriptionHistoryMapper;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.repository.user_activity.UserActivityRepository;
import com.sprint.part3.sb01_monew_team6.service.UserActivityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

	private final UserActivityRepository userActivityRepository;
	private final UserRepository userRepository;
	private final SubscriptionHistoryMapper subscriptionHistoryMapper;

	@Override
	public void addSubscriptionFromEvent(Long userId, SubscriptionHistoryDto subscriptionHistory) {
		userRepository.findById(userId)
			.orElseThrow(() -> new NotificationDomainException("유저를 찾을 수 없습니다.", Map.of("userId", userId)));

		userActivityRepository.addSubscription(userId, subscriptionHistoryMapper.fromDto(subscriptionHistory));
	}

	@Override
	public void addCommentLikeFromEvent(Long userId, CommentLikeHistoryDto commentLikeHistory) {

	}

	@Override
	public void addCommentFromEvent(Long userId, CommentHistoryDto commentHistory) {

	}

	@Override
	public void addArticleViewFromEvent(Long userId, ArticleViewHistoryDto articleViewHistory) {

	}
}

package com.sprint.part3.sb01_monew_team6.service.impl;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.UserActivityDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityException;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.ArticleViewHistoryMapper;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.CommentHistoryMapper;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.CommentLikeHistoryMapper;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.SubscriptionHistoryMapper;
import com.sprint.part3.sb01_monew_team6.mapper.user_activity.UserActivityMapper;
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
	private final CommentLikeHistoryMapper commentLikeHistoryMapper;
	private final CommentHistoryMapper commentHistoryMapper;
	private final ArticleViewHistoryMapper articleViewHistoryMapper;
	private final UserActivityMapper userActivityMapper;

	@Override
	public void addSubscriptionFromEvent(Long userId, SubscriptionHistoryDto subscriptionHistory) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.addSubscription(userId, subscriptionHistoryMapper.fromDto(subscriptionHistory));
	}

	@Override
	public void addCommentLikeFromEvent(Long userId, CommentLikeHistoryDto commentLikeHistory) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.addCommentLike(userId, commentLikeHistoryMapper.fromDto(commentLikeHistory));
	}

	@Override
	public void addCommentFromEvent(Long userId, CommentHistoryDto commentHistory) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.addComment(userId, commentHistoryMapper.fromDto(commentHistory));
	}

	@Override
	public void addArticleViewFromEvent(Long userId, ArticleViewHistoryDto articleViewHistory) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.addArticleView(userId, articleViewHistoryMapper.fromDto(articleViewHistory));
	}

	@Override
	public void removeSubscriptionFromEvent(Long userId, Long interestId) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.removeSubscription(userId, interestId);
	}

	@Override
	public void removeCommentLikeFromEvent(Long userId, Long commentId) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.removeCommentLike(userId, commentId);
	}

	@Override
	public void removeCommentFromEvent(Long userId, Long articleId) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.removeComment(userId, articleId);
	}

	@Override
	public void removeArticleViewFromEvent(Long userId, Long viewedBy) {
		validateUserIdOrThrowDomainException(userId);

		userActivityRepository.removeArticleView(userId, viewedBy);
	}

	@Override
	public UserActivityDto findByUserId(Long userId) {
		validateUserIdOrThrowApiException(userId);

		return userActivityRepository.findByUserId(userId)
			.map(userActivityMapper::toDto)
			.orElse(null);
	}

	private void validateUserIdOrThrowDomainException(Long userId) {
		if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
			throw new UserActivityDomainException("유저를 찾을 수 없습니다.", Map.of("userId", userId));
		}
	}

	private void validateUserIdOrThrowApiException(Long userId) {
		if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
			throw new UserActivityException(USER_ACTIVITY_NOT_FOUND_EXCEPTION, Instant.now(), BAD_REQUEST);
		}
	}
}

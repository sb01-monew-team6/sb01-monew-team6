package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;

public interface UserActivityService {

	void addSubscriptionFromEvent(Long userId, SubscriptionHistoryDto subscriptionHistory);

	void addCommentLikeFromEvent(Long userId, CommentLikeHistoryDto commentLikeHistory);

	void addCommentFromEvent(Long userId, CommentHistoryDto commentHistory);

	void addArticleViewFromEvent(Long userId, ArticleViewHistoryDto articleViewHistory);

	void removeSubscriptionFromEvent(Long userId, Long interestId);

	void removeCommentLikeFromEvent(Long userId, Long commentId);

	void removeCommentFromEvent(Long userId, Long articleId);
}

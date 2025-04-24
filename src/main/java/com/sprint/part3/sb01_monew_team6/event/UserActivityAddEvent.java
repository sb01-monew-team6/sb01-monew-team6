package com.sprint.part3.sb01_monew_team6.event;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.enums.UserActivityType;

public record UserActivityAddEvent(
	Long userId,
	UserActivityType type,
	SubscriptionHistoryDto subscription,
	CommentHistoryDto comment,
	CommentLikeHistoryDto commentLike,
	ArticleViewHistoryDto articleView
) {
}

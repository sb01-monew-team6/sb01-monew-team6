package com.sprint.part3.sb01_monew_team6.event;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;

public record UserActivityCreateEvent(
	Long userId,
	SubscriptionHistoryDto subscription,
	CommentHistoryDto comment,
	CommentLikeHistoryDto commentLike,
	ArticleViewHistoryDto articleView
) {
}

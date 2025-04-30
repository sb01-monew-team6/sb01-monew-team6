package com.sprint.part3.sb01_monew_team6.dto.user_activity;

import java.time.Instant;
import java.util.List;

public record UserActivityDto(
	Long id,
	String email,
	String nickname,
	Instant createdAt,
	List<SubscriptionHistoryDto> subscriptions,
	List<CommentLikeHistoryDto> commentLikes,
	List<CommentHistoryDto> comments,
	List<ArticleViewHistoryDto> articleViews
) {
}

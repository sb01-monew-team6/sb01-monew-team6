package com.sprint.part3.sb01_monew_team6.dto.user_activity;

import java.time.Instant;

public record CommentLikeHistoryDto(
	Long commentId,
	Long articleId,
	String articleTitle,
	Long commentUserId,
	String commentUserNickname,
	String commentContent,
	Long commentLikeCount,
	Instant commentCreatedAt,
	Instant createdAt
) {
}

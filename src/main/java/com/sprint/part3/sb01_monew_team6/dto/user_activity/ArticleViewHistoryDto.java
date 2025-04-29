package com.sprint.part3.sb01_monew_team6.dto.user_activity;

import java.time.Instant;
import java.time.LocalDateTime;

public record ArticleViewHistoryDto(
	Long viewedBy,
	Long articleId,
	String source,
	String sourceUrl,
	String articleTitle,
	LocalDateTime articlePublishedDate,
	String articleSummary,
	Long articleCommentCount,
	Long articleViewCount,
	Instant createdAt
) {
}

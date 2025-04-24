package com.sprint.part3.sb01_monew_team6.dto.user_activity;

public record CommentHistoryDto(
	Long articleId,
	String articleTitle,
	Long userId,
	String userNickname,
	String content,
	Long likeCount
) {
}

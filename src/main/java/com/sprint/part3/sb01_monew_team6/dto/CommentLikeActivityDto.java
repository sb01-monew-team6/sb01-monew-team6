package com.sprint.part3.sb01_monew_team6.dto;

import java.time.Instant;

public record CommentLikeActivityDto(
        Long id,
        Instant createdAt,
        Long commentId,
        Long articleId,
        String articleTitle,
        Long commentUserId,
        String commentUserNickname,
        long commentLikeCount,
        String commentContent,
        Instant commentCreatedAt
) {
}

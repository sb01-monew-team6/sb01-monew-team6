package com.sprint.part3.sb01_monew_team6.dto;

import java.time.Instant;

public record CommentLikeDto(
        Long id,
        Long likedBy,
        Instant createdAt,
        Long commentId,
        Long articleId,
        Long commentUserId,
        String commentUserNickname,
        String commentContent,
        long commentLikeCount,
        Instant commentCreatedAt
) {
}

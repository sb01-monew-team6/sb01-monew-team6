package com.sprint.part3.sb01_monew_team6.dto;

import java.time.Instant;

public record CommentActivityDto(
        Long id,
        Long articleId,
        String articleTitle,
        Long userId,
        String userNickname,
        String content,
        long likeCount,
        Instant createdAt
) {
}

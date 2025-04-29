package com.sprint.part3.sb01_monew_team6.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record CommentDto(
        Long id,
        Long articleId,
        Long userId,
        String userNickname,
        String content,
        long likeCount,
        boolean likedByMe,
        Instant createdAt
) {
}

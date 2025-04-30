package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.Comment;
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
    public static CommentDto fromEntity(Comment comment, long likeCount, boolean likedByMe) {
        return CommentDto.builder()
                .id(comment.getId())
                .articleId(comment.getArticle().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}

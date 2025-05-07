package com.sprint.part3.sb01_monew_team6.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CommentLikeDtoTest {
    
    @Test
    @DisplayName("모든 필드를 사용한 생성자 테스트")
    void constructor_shouldInitializeAllFieldsCorrectly() {
        // given
        Long id = 1L;
        Long likedBy = 2L;
        Instant createdAt = Instant.now();
        Long commentId = 3L;
        Long articleId = 4L;
        Long commentUserId = 5L;
        String commentUserNickname = "닉네임";
        String commentContent = "댓글 내용";
        long commentLikeCount = 7L;
        Instant commentCreatedAt = Instant.now();

        // when
        CommentLikeDto dto = new CommentLikeDto(
                id, likedBy, createdAt, commentId, articleId,
                commentUserId, commentUserNickname, commentContent,
                commentLikeCount, commentCreatedAt
        );

        // then
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.likedBy()).isEqualTo(likedBy);
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.commentId()).isEqualTo(commentId);
        assertThat(dto.articleId()).isEqualTo(articleId);
        assertThat(dto.commentUserId()).isEqualTo(commentUserId);
        assertThat(dto.commentUserNickname()).isEqualTo(commentUserNickname);
        assertThat(dto.commentContent()).isEqualTo(commentContent);
        assertThat(dto.commentLikeCount()).isEqualTo(commentLikeCount);
        assertThat(dto.commentCreatedAt()).isEqualTo(commentCreatedAt);
    }
}

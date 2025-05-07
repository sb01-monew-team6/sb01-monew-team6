package com.sprint.part3.sb01_monew_team6.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CommentLikeActivityDtoTest {

    @Test
    @DisplayName("CommentLikeActivityDto 생성자 테스트 - 필드 초기화 확인")
    void constructor_shouldInitializeAllFieldsCorrectly() {
        // given
        Long id = 1L;
        Instant createdAt = Instant.now();
        Long commentId = 2L;
        Long articleId = 3L;
        String articleTitle = "테스트 기사";
        Long commentUserId = 4L;
        String commentUserNickname = "댓글유저";
        long commentLikeCount = 5L;
        String commentContent = "댓글 내용";
        Instant commentCreatedAt = Instant.now();

        // when
        CommentLikeActivityDto dto = new CommentLikeActivityDto(
                id, createdAt, commentId, articleId, articleTitle,
                commentUserId, commentUserNickname, commentLikeCount,
                commentContent, commentCreatedAt
        );

        // then
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.commentId()).isEqualTo(commentId);
        assertThat(dto.articleId()).isEqualTo(articleId);
        assertThat(dto.articleTitle()).isEqualTo(articleTitle);
        assertThat(dto.commentUserId()).isEqualTo(commentUserId);
        assertThat(dto.commentUserNickname()).isEqualTo(commentUserNickname);
        assertThat(dto.commentLikeCount()).isEqualTo(commentLikeCount);
        assertThat(dto.commentContent()).isEqualTo(commentContent);
        assertThat(dto.commentCreatedAt()).isEqualTo(commentCreatedAt);
    }
}

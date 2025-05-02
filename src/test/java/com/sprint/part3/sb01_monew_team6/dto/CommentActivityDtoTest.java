package com.sprint.part3.sb01_monew_team6.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CommentActivityDtoTest {

    @Test
    @DisplayName("CommentActivityDto 생성자 테스트 - 필드 초기화 확인")
    void constructor_shouldInitializeAllFieldsCorrectly() {
        // given
        Long id = 1L;
        Long articleId = 2L;
        String articleTitle = "테스트 제목";
        Long userId = 3L;
        String userNickname = "유저닉";
        String content = "댓글 내용";
        long likeCount = 4L;
        Instant createdAt = Instant.now();

        // when
        CommentActivityDto dto = new CommentActivityDto(
                id, articleId, articleTitle,
                userId, userNickname, content,
                likeCount, createdAt
        );

        // then
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.articleId()).isEqualTo(articleId);
        assertThat(dto.articleTitle()).isEqualTo(articleTitle);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.userNickname()).isEqualTo(userNickname);
        assertThat(dto.content()).isEqualTo(content);
        assertThat(dto.likeCount()).isEqualTo(likeCount);
        assertThat(dto.createdAt()).isEqualTo(createdAt);
    }
}

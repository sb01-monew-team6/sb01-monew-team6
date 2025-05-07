package com.sprint.part3.sb01_monew_team6.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentRegisterRequestTest {

    @Test
    @DisplayName("record 필드 값이 정확히 할당되는지 확인")
    void fieldAssignment_shouldWorkCorrectly() {
        // given
        Long userId = 1L;
        Long articleId = 2L;
        String content = "댓글 테스트";

        // when
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, content);

        // then
        assertThat(request.articleId()).isEqualTo(2L);
        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.content()).isEqualTo("댓글 테스트");
    }

    @Test
    @DisplayName("equals, hashCode, toString 동작 확인")
    void basicMethods_shouldWorkAsExpected() {
        CommentRegisterRequest r1 = new CommentRegisterRequest(2L, 1L, "내용");
        CommentRegisterRequest r2 = new CommentRegisterRequest(2L, 1L, "내용");

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1.toString()).contains("내용", "1", "2");
    }
}

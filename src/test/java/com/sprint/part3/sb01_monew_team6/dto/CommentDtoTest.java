package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDtoTest {

    @Test
    @DisplayName("fromEntity: user, article 모두 있는 정상 케이스")
    void fromEntity_normal() {
        User user = createInstance(User.class);
        setField(user, "id", 1L);
        setField(user, "nickname", "테스터");

        NewsArticle newsArticle = createInstance(NewsArticle.class);
        setField(newsArticle, "id", 100L);

        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 10L);
        setField(comment, "content", "댓글 본문");
        setField(comment, "user", user);
        setField(comment, "article", newsArticle);
        setField(comment, "createdAt", Instant.parse("2024-05-01T00:00:00Z"));

        CommentDto dto = CommentDto.fromEntity(comment, 7L, true);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.articleId()).isEqualTo(100L);
        assertThat(dto.userId()).isEqualTo(1L);
        assertThat(dto.userNickname()).isEqualTo("테스터");
        assertThat(dto.content()).isEqualTo("댓글 본문");
        assertThat(dto.likeCount()).isEqualTo(7L);
        assertThat(dto.likedByMe()).isTrue();
        assertThat(dto.createdAt()).isEqualTo(Instant.parse("2024-05-01T00:00:00Z"));
    }

    @Test
    @DisplayName("fromEntity: user,article이 null인 경우도 정상 동작")
    void fromEntity_nullUserAndArticle() {
        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 11L);
        setField(comment, "content", "내용 없음");
        setField(comment, "createdAt", Instant.now());

        CommentDto dto = CommentDto.fromEntity(comment, 0L, false);

        assertThat(dto.articleId()).isNull();
        assertThat(dto.userId()).isNull();
        assertThat(dto.userNickname()).isNull();
        assertThat(dto.likeCount()).isZero();
        assertThat(dto.likedByMe()).isFalse();
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("객체 생성 실패: " + clazz.getSimpleName(), e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // BaseEntity의 필드에 접근하기 위해
            } catch (Exception e) {
                throw new RuntimeException(fieldName + " 설정 실패", e);
            }
        }
        throw new RuntimeException(fieldName + " 필드를 찾을 수 없음");
    }
}

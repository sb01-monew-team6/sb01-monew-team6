package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.Comment;

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

    @Test
    @DisplayName("fromEntity: user가 null이고 article만 있는 경우")
    void fromEntity_nullUser_onlyArticle() {
        NewsArticle article = createInstance(NewsArticle.class);
        setField(article, "id", 200L);

        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 12L);
        setField(comment, "content", "본문");
        setField(comment, "article", article);
        setField(comment, "createdAt", Instant.now());

        CommentDto dto = CommentDto.fromEntity(comment, 3L, false);

        assertThat(dto.userId()).isNull();
        assertThat(dto.userNickname()).isNull();
        assertThat(dto.articleId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("fromEntity: article이 null이고 user만 있는 경우")
    void fromEntity_nullArticle_onlyUser() {
        User user = createInstance(User.class);
        setField(user, "id", 2L);
        setField(user, "nickname", "닉네임");

        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 13L);
        setField(comment, "content", "본문2");
        setField(comment, "user", user);
        setField(comment, "createdAt", Instant.now());

        CommentDto dto = CommentDto.fromEntity(comment, 5L, true);

        assertThat(dto.articleId()).isNull();
        assertThat(dto.userId()).isEqualTo(2L);
        assertThat(dto.userNickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("record 생성자 통해 CommentDto 필드 확인")
    void recordCommentDto_fieldCheck() {
        Instant now = Instant.now();

        UserDto userDto = new UserDto(
                1L,
                "test@email.com",
                "테스트유저",
                now
        );

        CommentDto dto = new CommentDto(
                10L,
                100L,
                1L,
                "테스트유저",
                "테스트 댓글",
                3L,
                true,
                now
        );

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.articleId()).isEqualTo(100L);
        assertThat(dto.userId()).isEqualTo(1L);
        assertThat(dto.userNickname()).isEqualTo("테스트유저");
        assertThat(dto.content()).isEqualTo("테스트 댓글");
        assertThat(dto.likeCount()).isEqualTo(3L);
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.likedByMe()).isTrue();
    }

    @DisplayName("fromEntity: createdAt이 null이어도 정상 동작")
    @Test
    void fromEntity_createdAtIsNull_shouldNotFail() {
        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 14L);
        setField(comment, "content", "null createdAt 테스트");

        CommentDto dto = CommentDto.fromEntity(comment, 0L, false);

        assertThat(dto.createdAt()).isNull();
    }

    @DisplayName("fromEntity: content가 null이면 그대로 반영")
    @Test
    void fromEntity_contentIsNull_shouldSetNull() {
        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 15L);
        setField(comment, "content", null);

        CommentDto dto = CommentDto.fromEntity(comment, 0L, false);

        assertThat(dto.content()).isNull();
    }

    @DisplayName("fromEntity: likedByMe가 false인 경우 확인")
    @Test
    void fromEntity_likedByMeFalse_shouldReturnFalse() {
        Comment comment = createInstance(Comment.class);
        setField(comment, "id", 16L);
        setField(comment, "content", "좋아요 없음");

        CommentDto dto = CommentDto.fromEntity(comment, 0L, false);

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

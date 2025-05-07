package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.dto.CommentUpdateRequest;
import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotSoftDeletedException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @DisplayName("댓글 등록 - 정상 등록 시 CommentDto 반환")
    @Test
    void registerComment_shouldReturnCommentDto() {
        //  given
        CommentRegisterRequest request = CommentRegisterRequest.builder()
                .articleId(1L)
                .userId(1L)
                .content("테스트 댓글입니다.")
                .build();

        NewsArticle article = createTestArticle();
        User user = createTestUser();
        Comment comment = createTestComment(user, article, request.content());


        given(newsArticleRepository.findById(1L)).willReturn(Optional.of(article));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        //  when
        CommentDto result = commentService.register(request);

        //  then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("테스트 댓글입니다.");
    }

    @DisplayName("댓글 등록 - 잘못된 articleId일 경우 예외 발생")
    @Test
    void registerComment_withInvalidArticleId_shouldThrowNewsException() {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(999L, 1L, "테스트 댓글");

        // mock
        given(newsArticleRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(NewsException.class, () -> commentService.register(request));
    }

    @DisplayName("댓글 등록 - 잘못된 userId일 경우 예외 발생")
    @Test
    void registerComment_withInvalidUserId_shouldThrowNewsException() {
        // given
        CommentRegisterRequest request = CommentRegisterRequest.builder()
                .articleId(1L)
                .userId(999L)   // 존재하지 않는 userId
                .content("테스트 댓글")
                .build();

        // mock
        given(newsArticleRepository.findById(1L)).willReturn(Optional.of(createTestArticle()));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserException.class, () -> commentService.register(request));
    }

    @DisplayName("댓글 등록 실패 - content가 비어있을 경우 예외 발생")
    @Test
    void registerComment_withBlankContent_shouldThrowException() {
        CommentRegisterRequest request = new CommentRegisterRequest(1L, 1L, " ");
//        given(commentRepository.save(any(Comment.class))).willThrow(new IllegalArgumentException("Content cannot be empty"));

        assertThrows(NewsException.class, () -> commentService.register(request));
    }

    @DisplayName("댓글 등록 실패 - content가 너무 길 경우 예외 발생")
    @Test
    void registerComment_withTooLongContent_shouldThrowException() {
        // given
        String longContent = "a".repeat(1001); // 1001자
        CommentRegisterRequest request = new CommentRegisterRequest(1L, 1L, longContent);

        // when & then
        assertThrows(NewsException.class, () -> commentService.register(request));
    }

    @DisplayName("댓글 등록 실패 - articleId가 null일 경우 예외 발생")
    @Test
    void registerComment_withNullArticleId_shouldThrowException() {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(null, 1L, "테스트 댓글");

        // when & then
        assertThrows(NewsException.class, () -> commentService.register(request));
    }

    @DisplayName("댓글 목록 조회 실패 - 잘못된 orderBy일 경우 예외 발생")
    @Test
    void findAll_withInvalidOrderBy_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.findAll(1L, "invalidOrderBy", "ASC", null, null, 10, 1L);
        });
    }

    @DisplayName("댓글 목록 조회 실패 - 잘못된 direction일 경우 예외 발생")
    @Test
    void findAll_withInvalidDirection_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.findAll(1L, "createdAt", "INVALID", null, null, 10, 1L);
        });
    }

    @DisplayName("댓글 목록 조회 - 페이지네이션 적용 시 댓글 리스트 반환")
    @Test
    void findAll_withPagination_shouldReturnPagedComments() {
        // given
        Long articleId = 1L;
        String orderBy = "createdAt";
        String direction = "DESC";
        Integer limit = 2;
        Long requestUserId = 1L;

        // 댓글 객체 생성
        Comment comment1 = createTestComment(createTestUser(), createTestArticle(), "댓글 1");
        Comment comment2 = createTestComment(createTestUser(), createTestArticle(), "댓글 2");
        Comment comment3 = createTestComment(createTestUser(), createTestArticle(), "댓글 3");
        List<Comment> commentList = new ArrayList<>(List.of(comment1, comment2, comment3));

        // mock 설정
        given(newsArticleRepository.existsById(articleId)).willReturn(true);
        given(commentRepository.findAllByArticleId(articleId)).willReturn(commentList);
        given(commentLikeRepository.countByCommentId(comment1.getId())).willReturn(0L);
        given(commentLikeRepository.countByCommentId(comment2.getId())).willReturn(5L);
        given(commentLikeRepository.countByCommentId(comment3.getId())).willReturn(10L);
        given(commentLikeRepository.existsByCommentIdAndUserId(comment1.getId(), requestUserId)).willReturn(false);
        given(commentLikeRepository.existsByCommentIdAndUserId(comment2.getId(), requestUserId)).willReturn(true);
        given(commentLikeRepository.existsByCommentIdAndUserId(comment3.getId(), requestUserId)).willReturn(true);

        // when
        PageResponse<CommentDto> result = commentService.findAll(articleId, orderBy, direction, null, null, limit, requestUserId);
        List<CommentDto> contents = result.contents();

        // then
        assertThat(contents).isNotNull();
        assertThat(contents).hasSize(2);
        assertThat(contents.get(0).content()).isEqualTo("댓글 1");
        assertThat(contents.get(1).content()).isEqualTo("댓글 2");  // 두 번째 댓글
    }

    @DisplayName("댓글 목록 조회 - createdAt이 null인 댓글 포함 시 ASC 정렬 정상 처리")
    @Test
    void findAll_withNullCreatedAt_shouldSortCorrectly_ASC() {
        Comment nullCreated = commentWithCreatedAt(null);
        Comment validCreated = commentWithCreatedAt(Instant.now());

        given(newsArticleRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findAllByArticleId(1L)).willReturn(new ArrayList<>(List.of(validCreated, nullCreated)));
        given(commentLikeRepository.countByCommentId(any())).willReturn(0L);
        given(commentLikeRepository.existsByCommentIdAndUserId(any(), any())).willReturn(false);

        PageResponse<CommentDto> response = commentService.findAll(1L, "createdAt", "ASC", null, null, 10, 1L);
        List<CommentDto> result = response.contents();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).createdAt()).isNull(); // null createdAt이 먼저 나와야 ASC 정렬 성공
    }

    @DisplayName("댓글 목록 조회 - limit이 null이면 기본값 10개로 제한")
    @Test
    void findAll_withNullLimit_shouldDefaultTo10() {
        // given
        Long articleId = 1L;
        String orderBy = "createdAt";
        String direction = "ASC";
        Long requestUserId = 1L;

        // 11개 댓글 생성
        List<Comment> commentList = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Comment comment = createTestComment(createTestUser(), createTestArticle(), "댓글 " + i);
            commentList.add(comment);
        }

        given(newsArticleRepository.existsById(articleId)).willReturn(true);
        given(commentRepository.findAllByArticleId(articleId)).willReturn(commentList);
        given(commentLikeRepository.countByCommentId(any())).willReturn(0L);
        given(commentLikeRepository.existsByCommentIdAndUserId(any(), any())).willReturn(false);

        // when
        PageResponse<CommentDto> response = commentService.findAll(articleId, orderBy, direction, null, null, null, requestUserId);
        List<CommentDto> result = response.contents();

        // then
        assertThat(result).hasSize(10); // 기본 limit = 10
    }

    @DisplayName("댓글 목록 조회 - likeCount 기준 DESC 정렬")
    @Test
    void findAll_sortByLikeCountDesc_shouldSortProperly() {
        // 댓글 3개 생성 (내용만 다르고 ID는 아래에서 강제 설정)
        Comment commentLow = createTestComment(createTestUser(), createTestArticle(), "댓글 1");
        Comment commentMid = createTestComment(createTestUser(), createTestArticle(), "댓글 2");
        Comment commentHigh = createTestComment(createTestUser(), createTestArticle(), "댓글 3");

        // ID 수동 세팅 (Mockito 매칭 위해 필요)
        forceSetId(commentLow, 1L);
        forceSetId(commentMid, 2L);
        forceSetId(commentHigh, 3L);

        System.out.println("commentHigh ID (리플렉션) = " + getId(commentHigh));
        System.out.println("commentHigh.getId() = " + commentHigh.getId());

        // 확인용 로그
        System.out.println("commentHigh ID = " + getId(commentHigh));

        given(newsArticleRepository.existsById(1L)).willReturn(true);

        given(commentRepository.findAllByArticleId(1L))
                .willReturn(new ArrayList<>(List.of(commentHigh, commentMid, commentLow)));

        given(commentLikeRepository.countByCommentId(3L)).willReturn(10L);
        given(commentLikeRepository.countByCommentId(2L)).willReturn(5L);
        given(commentLikeRepository.countByCommentId(1L)).willReturn(0L);

        given(commentLikeRepository.existsByCommentIdAndUserId(any(), any())).willReturn(false);

        PageResponse<CommentDto> response = commentService.findAll(1L, "likeCount", "DESC", null, null, 10, 1L);
        List<CommentDto> result = response.contents();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).likeCount()).isEqualTo(10L); // commentHigh
        assertThat(result.get(1).likeCount()).isEqualTo(5L);  // commentMid
        assertThat(result.get(2).likeCount()).isEqualTo(0L);  // commentLow
    }

    @DisplayName("댓글 목록 조회 - createdAt 기준 DESC 정렬")
    @Test
    void findAll_sortByCreatedAtDesc_shouldSortProperly() {
        Comment c1 = createTestComment(createTestUser(), createTestArticle(), "첫 댓글");
        Comment c2 = createTestComment(createTestUser(), createTestArticle(), "두 번째 댓글");
        Comment c3 = createTestComment(createTestUser(), createTestArticle(), "세 번째 댓글");

        forceSetId(c1, 1L);
        forceSetId(c2, 2L);
        forceSetId(c3, 3L);

        // 날짜 수동 설정
        forceSetCreatedAt(c1, Instant.parse("2024-05-01T10:00:00Z"));
        forceSetCreatedAt(c2, Instant.parse("2024-05-01T12:00:00Z"));
        forceSetCreatedAt(c3, Instant.parse("2024-05-01T11:00:00Z"));

        given(newsArticleRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findAllByArticleId(1L)).willReturn(new ArrayList<>(List.of(c1, c2, c3)));
        given(commentLikeRepository.countByCommentId(anyLong())).willReturn(0L, 0L, 0L);
        given(commentLikeRepository.existsByCommentIdAndUserId(any(), any())).willReturn(false);

        PageResponse<CommentDto> response = commentService.findAll(1L, "createdAt", "DESC", null, null, 10, 1L);
        List<CommentDto> result = response.contents();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).content()).isEqualTo("두 번째 댓글");
        assertThat(result.get(1).content()).isEqualTo("세 번째 댓글");
        assertThat(result.get(2).content()).isEqualTo("첫 댓글");
    }

    @DisplayName("댓글 목록 조회 - 댓글이 하나도 없을 경우 빈 리스트 반환")
    @Test
    void findAll_withNoComments_shouldReturnEmptyList() {
        // given
        Long articleId = 1L;
        given(newsArticleRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findAllByArticleId(1L)).willReturn(new ArrayList<>());

        // when
        PageResponse<CommentDto> response = commentService.findAll(articleId, "createdAt", "ASC", null, null, 10, 1L);
        List<CommentDto> result = response.contents();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @DisplayName("댓글 목록 조회 - limit보다 적은 수의 댓글만 있을 경우 정상 반환")
    @Test
    void findAll_withLessThanLimitComments_shouldReturnAll() {

        // given
        Long articleId = 1L;
        Integer limit = 5;

        Comment c1 = createTestComment(createTestUser(), createTestArticle(), "댓글 1");
        Comment c2 = createTestComment(createTestUser(), createTestArticle(), "댓글 2");

        // createdAt 값을 명시적으로 설정하여 정렬 기준 통제
        forceSetCreatedAt(c1, Instant.parse("2024-05-01T10:00:00Z"));
        forceSetCreatedAt(c2, Instant.parse("2024-05-01T11:00:00Z"));

        List<Comment> comments = new ArrayList<>(List.of(c1, c2));

        given(newsArticleRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findAllByArticleId(1L)).willReturn(comments);
        given(commentLikeRepository.countByCommentId(any())).willReturn(0L);
        given(commentLikeRepository.existsByCommentIdAndUserId(any(), any())).willReturn(false);

        // when
        PageResponse<CommentDto> response = commentService.findAll(articleId, "createdAt", "ASC", null, null, limit, 1L);
        List<CommentDto> result = response.contents();

        // then
        assertThat(result).hasSize(2); // limit = 10이지만 댓글은 2개
        assertThat(result.get(0).content()).isEqualTo("댓글 1");
        assertThat(result.get(1).content()).isEqualTo("댓글 2");
    }

    private NewsArticle createTestArticle() {
        NewsArticle article = new NewsArticle();
        article.setSource("테스트 출처");
        article.setSourceUrl("http://test.com");
        article.setArticleTitle("테스트 제목");
        article.setArticlePublishedDate(Instant.now());
        article.setArticleSummary("테스트 요약");
        return article;
    }

    private User createTestUser() {
        return User.builder()
                .nickname("tester")
                .email("tester@example.com")
                .password("encodedPassword")
                .build();
    }

    private Comment createTestComment(User user, NewsArticle article, String content) {
        return Comment.builder()
                .user(user)
                .article(article)
                .content(content)
                .build();
    }
//    -----------------------------------------------------------------------------------------------------------------------------------------------------------
@DisplayName("댓글 논리 삭제 - 삭제요청시 삭제 정상응답확인")
@Test
void deleteComment_marksDeleted_true() {
    // given
    Long commentId = 1L;
    User user = createUser(2L);
    NewsArticle article = createArticle(3L);
    Comment comment = createComment(commentId, user, article, "삭제할 댓글", false);

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when
    commentService.softDeleteComment(commentId);

    // then
    assertThat(comment.isDeleted()).isTrue();
    verify(commentRepository).findById(commentId);
}

    @DisplayName("댓글 수정 - 내용 수정 시 CommentDto 반환")
    @Test
    void shouldUpdateContentAndReturnDto() {
        // given
        Long commentId = 1L;
        Long userId = 42L;
        String updatedContent = "수정된 댓글";

        User user = createUser(userId);
        NewsArticle article = createArticle(100L);
        Comment comment = createComment(commentId, user, article, "원래 댓글", false);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);

        // when
        CommentDto result = commentService.updateComment(commentId, userId, request);

        // then
        assertEquals(updatedContent, result.content());
        assertEquals(commentId, result.id());
        assertEquals(userId, result.userId());
    }

    @DisplayName("댓글 물리 삭제 - isDeleted가 true일 때 정상 삭제")
    @Test
    void shouldDeleteCommentIfSoftDeleted() {
        Long commentId = 1L;
        Comment comment = createComment(commentId, createUser(10L), createArticle(100L), "삭제 대상 댓글", true);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        commentService.hardDelete(commentId);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @DisplayName("댓글 물리 삭제 실패 - isDeleted가 false일 경우 예외 발생")
    @Test
    void shouldThrowExceptionIfNotSoftDeleted() {
        Long commentId = 1L;
        Comment comment = createComment(commentId, createUser(10L), createArticle(100L), "삭제 대상 댓글", false);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        org.junit.jupiter.api.Assertions.assertThrows(CommentNotSoftDeletedException.class, () -> {
            commentService.hardDelete(commentId);
        });
    }

    private User createUser(Long id) {
        User user = User.builder()
            .nickname("tester")
            .email("test@example.com")
            .password("1234")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private NewsArticle createArticle(Long id) {
        NewsArticle article = NewsArticle.builder()
            .source("naver")
            .sourceUrl("https://news.com")
            .articleTitle("테스트 기사")
            .articleSummary("요약")
            .articlePublishedDate(Instant.now())
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        return article;
    }

    private Comment createComment(Long id, User user, NewsArticle article, String content, boolean isDeleted) {
        Comment comment = Comment.builder()
            .user(user)
            .article(article)
            .content(content)
            .isDeleted(isDeleted)
            .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    private Comment commentWithCreatedAt(Instant createdAt) {
        Comment comment = Comment.builder()
                .user(createTestUser())
                .article(createTestArticle())
                .content("댓글")
                .build();

        try {
            Field field = Comment.class
                    .getSuperclass()
                    .getSuperclass()
                    .getDeclaredField("createdAt");

            field.setAccessible(true);
            field.set(comment, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("createdAt 강제 설정 실패", e);
        }

        return comment;
    }

    private void forceSetId(Comment comment, Long id) {
        try {
            Field idField = Comment.class
                    .getSuperclass()       // BaseUpdatableEntity
                    .getSuperclass()       // BaseEntity
                    .getDeclaredField("id");

            idField.setAccessible(true);
            idField.set(comment, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    private Long getId(Comment comment) {
        try {
            Field idField = Comment.class
                    .getSuperclass()
                    .getSuperclass()
                    .getDeclaredField("id");

            idField.setAccessible(true);
            return (Long) idField.get(comment);
        } catch (Exception e) {
            throw new RuntimeException("ID 조회 실패", e);
        }
    }

    private void forceSetCreatedAt(Comment comment, Instant time) {
        try {
            Class<?> clazz = comment.getClass();
            while (clazz != null) {
                try {
                    Field createdAtField = clazz.getDeclaredField("createdAt");
                    createdAtField.setAccessible(true);
                    createdAtField.set(comment, time);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass(); // 한 단계 위로 올라가서 다시 시도
                }
            }
            throw new RuntimeException("createdAt 필드를 찾을 수 없습니다.");
        } catch (Exception e) {
            throw new RuntimeException("createdAt 설정 실패", e);
        }
    }
}

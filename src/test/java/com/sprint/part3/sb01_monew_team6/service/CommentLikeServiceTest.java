package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentLikeDto;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.CommentLike;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityRemoveEvent;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.CommentLikeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class CommentLikeServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentLikeServiceImpl commentLikeService;

    @Captor
    private ArgumentCaptor<NotificationCreateEvent> notificationEventCaptor;

    @Captor
    private ArgumentCaptor<UserActivityAddEvent> userActivityAddEventCaptor;

    @Captor
    private ArgumentCaptor<UserActivityRemoveEvent> userActivityRemoveEventCaptor;

    public CommentLikeServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("댓글 좋아요 등록 - 정상 등록 시 CommentLikeDto 반환")
    void likeComment_shouldReturnCommentLikeDto() {
        // given
        Long commentId = 1L;
        Long userId = 100L;

        User testUser = createTestUser(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        Comment testComment = createTestComment(commentId, testUser, "댓글입니다");
        given(commentRepository.findByIdWithArticleAndCommentLikesAndUser(commentId)).willReturn(Optional.of(testComment));

        given(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)).willReturn(false);
        given(commentLikeRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(commentLikeRepository.countByCommentId(commentId)).willReturn(1L);

        // when
        CommentLikeDto result = commentLikeService.likeComment(commentId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.commentId()).isEqualTo(commentId);
        assertThat(result.commentUserId()).isEqualTo(userId);
        assertThat(result.commentLikeCount()).isEqualTo(1L);
        assertThat(result.likedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("댓글 좋아요 등록 실패 - 존재하지 않는 댓글 ID")
    void likeComment_withInvalidCommentId_shouldThrowException() {
        Long invalidId = 999L;

        given(commentRepository.findById(invalidId)).willReturn(Optional.empty());

        assertThrows(CommentException.class, () -> commentLikeService.likeComment(invalidId, 1L));
    }

    @Test
    @DisplayName("댓글 좋아요 등록 실패 - 존재하지 않는 사용자 ID")
    void likeComment_withInvalidUserId_shouldThrowException() {
        Comment testComment = createTestComment(1L, null, "댓글");

        given(commentRepository.findByIdWithArticleAndCommentLikesAndUser(1L)).willReturn(Optional.of(testComment));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(UserException.class, () -> commentLikeService.likeComment(1L, 999L));
    }

    @Test
    @DisplayName("댓글 좋아요 등록 실패 - 이미 좋아요한 경우 예외 발생")
    void likeComment_whenAlreadyLiked_shouldThrowException() {
        User testUser = createTestUser(1L);
        Comment testComment = createTestComment(1L, testUser, "댓글");

        given(commentRepository.findById(1L)).willReturn(Optional.of(testComment));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(commentLikeRepository.existsByCommentIdAndUserId(1L, 1L)).willReturn(true);

        assertThrows(CommentException.class, () -> commentLikeService.likeComment(1L, 1L));
    }

    @DisplayName("댓글 좋아요 취소 실패 - 좋아요한 적 없을 경우 예외 발생")
    @Test
    void cancelLike_shouldThrowException_whenLikeNotExists() {
        // given
        Long commentId = 1L;
        Long userId = 2L;

        given(commentLikeRepository.findByCommentIdAndUserId(userId, commentId))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(CommentException.class, () -> commentLikeService.cancelLike(commentId, userId));
    }

    private User createTestUser(Long id) {
        User user = User.builder()
                .nickname("사용자")
                .email("user@example.com")
                .password("password")
                .build();
        forceSetId(user, id);
        return user;
    }

    private Comment createTestComment(Long id, User user, String content) {
        Comment comment = Comment.builder()
                .content(content)
                .article(new NewsArticle())
                .commentLikes(Collections.emptyList())
                .user(user)
                .build();
        forceSetId(comment, id);
        return comment;
    }

    private void forceSetId(Object target, Long id) {
        try {
            Field idField = target.getClass()
                    .getSuperclass()
                    .getSuperclass()
                    .getDeclaredField("id");

            idField.setAccessible(true);
            idField.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    @Test
    @DisplayName("댓글 좋아요 정상 호출 시 알림 이벤트 정상 발생")
    public void publishNotification() throws Exception {
        // given
        Long commentId = 1L;
        Long userId = 100L;

        User testUser = createTestUser(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        Comment testComment = createTestComment(commentId, testUser, "댓글입니다");
        given(commentRepository.findByIdWithArticleAndCommentLikesAndUser(commentId)).willReturn(Optional.of(testComment));

        given(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)).willReturn(false);
        given(commentLikeRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(commentLikeRepository.countByCommentId(commentId)).willReturn(1L);

        // when
        commentLikeService.likeComment(commentId, userId);

        // then
        verify(eventPublisher, times(1)).publishEvent(notificationEventCaptor.capture());
        NotificationCreateEvent published = notificationEventCaptor.getValue();

        assertThat(published.resourceId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("댓글 좋아요 정상 호출 시 유저 활동 내역 댓글 좋아요 추가")
    public void addCommentLike() throws Exception {
        // given
        Long commentId = 1L;
        Long userId = 100L;

        User testUser = createTestUser(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        Comment testComment = Comment.builder()
            .user(testUser)
            .commentLikes(Collections.emptyList())
            .article(new NewsArticle())
            .build();
        given(commentRepository.findByIdWithArticleAndCommentLikesAndUser(commentId)).willReturn(Optional.of(testComment));

        CommentLike stub = CommentLike.builder()
            .user(testUser)
            .comment(testComment)
            .build();

        given(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)).willReturn(false);
        given(commentLikeRepository.save(any())).willReturn(stub);
        given(commentLikeRepository.countByCommentId(commentId)).willReturn(1L);
        doNothing().when(eventPublisher).publishEvent(any(NotificationCreateEvent.class));

        // when
        commentLikeService.likeComment(commentId, userId);

        // then
        verify(eventPublisher, times(1)).publishEvent(userActivityAddEventCaptor.capture());
        UserActivityAddEvent published = userActivityAddEventCaptor.getValue();

        assertThat(published.userId()).isEqualTo(userId);
        assertThat(published.commentLike().commentUserId()).isEqualTo(testComment.getUser().getId());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 정상 호출 시 유저 활동 내역 댓글 좋아요 삭제")
    public void removeCommentLike() throws Exception {
        // given
        Long commentId = 1L;
        Long userId = 100L;

        User testUser = createTestUser(userId);

        Comment testComment = Comment.builder()
            .user(testUser)
            .commentLikes(Collections.emptyList())
            .article(new NewsArticle())
            .build();

        CommentLike stub = CommentLike.builder()
            .user(testUser)
            .comment(testComment)
            .build();

        given(commentLikeRepository.findByCommentIdAndUserId(commentId, userId)).willReturn(Optional.of(stub));

        // when
        commentLikeService.cancelLike(commentId, userId);

        // then
        verify(eventPublisher, times(1)).publishEvent(userActivityRemoveEventCaptor.capture());
        UserActivityRemoveEvent published = userActivityRemoveEventCaptor.getValue();

        assertThat(published.userId()).isEqualTo(userId);
        assertThat(published.articleId()).isEqualTo(testComment.getArticle().getId());
    }
}

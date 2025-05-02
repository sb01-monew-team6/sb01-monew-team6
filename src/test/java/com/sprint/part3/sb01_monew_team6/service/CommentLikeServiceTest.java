package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentLikeDto;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.CommentLikeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

class CommentLikeServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @InjectMocks
    private CommentLikeServiceImpl commentLikeService;

    public CommentLikeServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("댓글 좋아요 등록 - 정상 등록 시 CommentLikeDto 반환")
    void likeComment_shouldReturnCommentLikeDto() {
        // given
        Long commentId = 1L;
        Long userId = 100L;

        User testUser = User.builder()
                .nickname("사용자")
                .email("user@example.com")
                .password("password")
                .build();
        forceSetUserId(testUser, userId);

        Comment comment = Comment.builder()
                .content("댓글입니다")
                .user(testUser)
                .build();
        forceSetCommentId(comment, commentId);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)).willReturn(false);
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
        Comment comment = Comment.builder()
                .content("댓글")
                .build();
        forceSetCommentId(comment, 1L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(UserException.class, () -> commentLikeService.likeComment(1L, 999L));
    }

    @Test
    @DisplayName("댓글 좋아요 등록 실패 - 이미 좋아요한 경우 예외 발생")
    void likeComment_whenAlreadyLiked_shouldThrowException() {
        User testUser = User.builder()
                .nickname("사용자")
                .email("user@example.com")
                .password("password")
                .build();
        forceSetUserId(testUser, 1L);

        Comment comment = Comment.builder()
                .content("댓글")
                .user(testUser)
                .build();
        forceSetCommentId(comment, 1L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(commentLikeRepository.existsByCommentIdAndUserId(1L, 1L)).willReturn(true);

        assertThrows(CommentException.class, () -> commentLikeService.likeComment(1L, 1L));
    }

    private void forceSetCommentId(Comment comment, Long id) {
        try {
            Field idField = Comment.class
                    .getSuperclass()
                    .getSuperclass()
                    .getDeclaredField("id");

            idField.setAccessible(true);
            idField.set(comment, id);
        } catch (Exception e) {
            throw new RuntimeException("댓글 ID 설정 실패", e);
        }
    }

    private void forceSetUserId(User user, Long id) {
        try {
            Field idField = User.class
                    .getSuperclass()
                    .getSuperclass()
                    .getDeclaredField("id");

            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("유저 ID 설정 실패", e);
        }
    }
}

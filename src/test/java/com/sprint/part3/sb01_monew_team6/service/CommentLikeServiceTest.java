//package com.sprint.part3.sb01_monew_team6.service;
//
//import com.sprint.part3.sb01_monew_team6.dto.UserDto;
//import com.sprint.part3.sb01_monew_team6.entity.Comment;
//import com.sprint.part3.sb01_monew_team6.entity.User;
//import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
//import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
//import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
//import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
//import com.sprint.part3.sb01_monew_team6.service.impl.CommentLikeServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.Instant;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class CommentLikeServiceTest {
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private CommentLikeRepository commentLikeRepository;
//
//    @InjectMocks
//    private CommentLikeServiceImpl commentLikeService;
//
//    private Comment comment;
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this); // Mockito 모킹 객체 초기화
//
//        // 테스트를 위한 데이터 준비
//        comment = new Comment();
//        comment.setId(1L);
//        comment.setContent("좋아요 테스트 댓글");
//
//        // userDto 객체를 사용하여 User 엔티티 생성
//        UserDto userDto = new UserDto(1L, "tester@example", "tester", Instant.now());
//        user = UserDto.fromEntity(userDto); //  UserDto를 User 엔티티로 변환
//    }
//
//    @DisplayName("댓글 좋아요 추가 시 CommentLike 저장")
//    @Test
//    void addCommentLike_shouldSaveLike() {
//        // given
//        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
//        given(userRepository.findById(1L)).willReturn(Optional.of(user));
//
//        // when
//        commentLikeService.toggleCommentLike(1L, 1L);
//
//        // then
//        assertThat(commentLikeRepository.existsByCommentIdAndUserId(1L, 1L)).isTrue();
//    }
//
//    @DisplayName("댓글 좋아요 취소 시 CommentLike 삭제")
//    @Test
//    void removeCommentLike_shouldRemoveLike() {
//        // given
//        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
//        given(userRepository.findById(1L)).willReturn(Optional.of(user));
//        given(commentLikeRepository.existsByCommentIdAndUserId(1L, 1L)).willReturn(true);
//
//        // when
//        commentLikeService.toggleCommentLike(1L, 1L);
//
//        // then
//        assertThat(commentLikeRepository.existsByCommentIdAndUserId(1L, 1L)).isFalse();
//    }
//
//    @DisplayName("댓글이 존재하지 않으면 예외 처리")
//    @Test
//    void addCommentLike_shouldThrowExceptionIfCommentNotFound() {
//        // given
//        given(commentRepository.findById(999L)).willReturn(Optional.empty());
//
//        // when & then
//        assertThrows(CommentNotFoundException.class, () -> {
//            commentLikeService.toggleCommentLike(999L, 1L);
//        });
//    }
//
//    @DisplayName("사용자가 존재하지 않으면 예외 처리")
//    @Test
//    void addCommentLike_shouldThrowExceptionIfUserNotFound() {
//        // given
//        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
//        given(userRepository.findById(999L)).willReturn(Optional.empty());
//
//        // when & then
//        assertThrows(UserNotFoundException.class, () -> {
//            commentLikeService.toggleCommentLike(1L, 999L);
//        });
//    }
//}

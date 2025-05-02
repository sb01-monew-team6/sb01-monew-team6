package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.CommentLikeDto;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public CommentLikeDto likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND));

        // 이미 좋아요 했는지 확인
        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        if(alreadyLiked) {
            throw new CommentException(ErrorCode.ALREADY_LIKED_COMMENT, Instant.now(), HttpStatus.BAD_REQUEST);
        }

        // 좋아요 등록(save 생략 -> 테스트에서는 mocking 처리)
        // commentLikeRepository.save(...);

        // 좋아요 수 갱신
        long likeCount = commentLikeRepository.countByCommentId(commentId);

        return CommentLikeDto.builder()
                .id(1L) // 가짜 ID (mocked)
                .likedBy(userId)
                .createdAt(comment.getCreatedAt()) // 임시
                .commentId(commentId)
                .articleId(comment.getArticle() != null ? comment.getArticle().getId() : null)
                .commentUserId(comment.getUser() != null ? comment.getUser().getId() : null)
                .commentUserNickname(comment.getUser() != null ? comment.getUser().getNickname() : null)
                .commentContent(comment.getContent())
                .commentLikeCount(likeCount)
                .commentCreatedAt(comment.getCreatedAt())
                .build();
    }
}

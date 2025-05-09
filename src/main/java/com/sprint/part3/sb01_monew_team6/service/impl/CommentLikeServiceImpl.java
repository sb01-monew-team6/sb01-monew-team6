package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.CommentLikeDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.CommentLike;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.entity.enums.UserActivityType;
import com.sprint.part3.sb01_monew_team6.event.NotificationCreateEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityRemoveEvent;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentLikeDto likeComment(Long commentId, Long userId) {
        log.info("[likeComment] 댓글 좋아요 처리 시작: commentId={}, userId={}", commentId, userId);
        Comment comment = commentRepository.findByIdWithArticleAndCommentLikesAndUser(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND));

        // 이미 좋아요 했는지 확인
        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        if(alreadyLiked) {
            throw new CommentException(ErrorCode.ALREADY_LIKED_COMMENT, Instant.now(), HttpStatus.BAD_REQUEST);
        }

        CommentLike commentLike = commentLikeRepository.save(CommentLike.of(comment, user));

        // 좋아요 수 갱신
        long likeCount = commentLikeRepository.countByCommentId(commentId);

        log.info("[likeComment] 좋아요 완료: commentLikeId={}, commentId={}, userId={}", commentLike.getId(), commentId, userId);

        publishNotification(comment.getUser().getId(), userId);

        publishUserActivityAddEvent(userId, comment, user, commentLike);

        return CommentLikeDto.builder()
                .id(commentLike.getId())
                .likedBy(userId)
                .createdAt(commentLike.getCreatedAt())
                .commentId(commentId)
                .articleId(comment.getArticle() != null ? comment.getArticle().getId() : null)
                .commentUserId(comment.getUser() != null ? comment.getUser().getId() : null)
                .commentUserNickname(comment.getUser() != null ? comment.getUser().getNickname() : null)
                .commentContent(comment.getContent())
                .commentLikeCount(likeCount)
                .commentCreatedAt(comment.getCreatedAt())
                .build();
    }

    private void publishUserActivityAddEvent(Long userId, Comment comment, User user, CommentLike commentLike) {
        UserActivityAddEvent event = new UserActivityAddEvent(
            userId,
            UserActivityType.COMMENT_LIKE,
            null,
            null,
            new CommentLikeHistoryDto(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getArticle().getArticleTitle(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                (long)comment.getCommentLikes().size(),
                comment.getCreatedAt(),
                commentLike.getCreatedAt()
            ),
            null
        );
        eventPublisher.publishEvent(event);
    }

    private void publishNotification(Long commentUserId, Long userId) {
        NotificationCreateEvent event = new NotificationCreateEvent(
            commentUserId,
            userId,
            ResourceType.COMMENT,
            null,
            null
        );
        eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional
    public void cancelLike(Long commentId, Long userId) {
        log.info("[cancelLike] 좋아요 취소 요청: commentId={}, userId={}", commentId, userId);
        Optional<CommentLike> optional = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
        if (optional.isPresent()) {
            commentLikeRepository.delete(optional.get());
            log.info("[cancelLike] 좋아요 취소 완료: commentId={}, userId={}", commentId, userId);

            publishUserActivityRemoveEvent(userId, commentId);
        } else {
            log.warn("[cancelLike] 좋아요 기록 없음: commentId={}, userId={}", commentId, userId);
            throw new CommentException(ErrorCode.COMMENT_LIKE_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND);
        }
    }

    private void publishUserActivityRemoveEvent(Long userId, Long commentId) {
        UserActivityRemoveEvent event = new UserActivityRemoveEvent(
            userId,
            UserActivityType.COMMENT_LIKE,
            null,
            commentId,
            null,
            null
        );
        eventPublisher.publishEvent(event);
    }
}

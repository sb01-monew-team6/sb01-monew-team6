package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.*;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.UserActivityType;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.event.UserActivityRemoveEvent;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentException;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotSoftDeletedException;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.CommentLikeService;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentLikeService commentLikeService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CommentDto register(CommentRegisterRequest request) {
        log.info("[registerComment] 댓글 등록 요청: userId={}, articleId={}", request.userId(), request.articleId());
        //  1. Article 조회
        NewsArticle article = newsArticleRepository.findById(request.articleId())
                .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND));

        //  2. User 조회
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, Instant.now(), HttpStatus.NOT_FOUND));

        //  3. Comment 생성
        Comment comment = Comment.builder()
                .article(article)
                .user(user)
                .content(request.content())
                .build();

        //  4. 저장
        Comment savedComment = commentRepository.save(comment);
        log.info("[register] 댓글 저장 완료: commentId={}", savedComment.getId());

        publishUserActivityAddEvent(article, user, savedComment);

        //  5. 저장된 데이터를 CommentDto로 변환 후 반환
        return CommentDto.builder()
                .id(savedComment.getId())
                .articleId(savedComment.getArticle().getId())
                .userId(savedComment.getUser().getId())
                .userNickname(user.getNickname()) //  작성자 이름
                .content(savedComment.getContent())
                .likeCount(0L)
                .likedByMe(false)
                .createdAt(savedComment.getCreatedAt())
                .build();
    }

    private void publishUserActivityAddEvent(NewsArticle article, User user, Comment savedComment) {
        UserActivityAddEvent event = new UserActivityAddEvent(
            user.getId(),
            UserActivityType.COMMENT,
            null,
            new CommentHistoryDto(
                article.getId(),
                article.getArticleTitle(),
                user.getId(),
                user.getNickname(),
                savedComment.getContent(),
                0L,
                savedComment.getCreatedAt()
            ),
            null,
            null
        );
        eventPublisher.publishEvent(event);
    }

    @Override
    public PageResponse<CommentDto> findAll(
            Long articleId,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit,
            Long requestUserId
    ) {
        log.info("[findAll] 댓글 목록 조회 요청: articleId={}, orderBy={}, direction={}, limit={}, userId={}",
                articleId, orderBy, direction, limit, requestUserId);
        // 1. orderBy 값 검증
        if (!orderBy.equals("createdAt") && !orderBy.equals("likeCount")) {
            throw new IllegalArgumentException("Invalid orderBy value");
        }

        // 2. direction 값 검증
        if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Invalid direction value");
        }

        // 3. 댓글 목록 조회
        List<Comment> comments;
        if (articleId != null) {
            boolean exists = newsArticleRepository.existsById(articleId);
            if (!exists) {
                throw new CommentException(ErrorCode.COMMENT_NOT_FOUND, Instant.now(), HttpStatus.BAD_REQUEST);
            }
            comments = commentRepository.findAllByArticleId(articleId);
        } else {
            comments = commentRepository.findAll(); // 특정 게시글 ID가 없으면 전체 댓글 조회
        }

        // 4. 댓글 정렬
        comments.sort((c1, c2) -> {
            int result;
            if (orderBy.equals("createdAt")) {
                result = compareInstant(c1.getCreatedAt(), c2.getCreatedAt());
            } else {
                long l1 = commentLikeRepository.countByCommentId(c1.getId());
                long l2 = commentLikeRepository.countByCommentId(c2.getId());
                result = Long.compare(l1, l2);
            }
            return direction.equalsIgnoreCase("DESC") ? -result : result;
        });

        // 커서 필터링
        if (cursor != null && after != null) {
            boolean isAfter = Boolean.parseBoolean(after);
            comments = comments.stream()
                    .filter(c -> {
                        if (orderBy.equals("createdAt")) {
                            Instant created = c.getCreatedAt();
                            Instant cur = Instant.parse(cursor);
                            return isAfter ? created.isAfter(cur) : created.isBefore(cur);
                        } else {
                            long count = commentLikeRepository.countByCommentId(c.getId());
                            long cur = Long.parseLong(cursor);
                            return isAfter ? count > cur : count < cur;
                        }
                    })
                    .toList();
        }

        // 페이징 적용
        int max = limit != null ? limit : 10;
        List<Comment> paged = comments.stream().limit(max + 1).toList(); // hasNext 판별 위해 +1

        // DTO 변환
        List<CommentDto> dtos = paged.stream()
                .limit(max)
                .map(c -> CommentDto.fromEntity(
                        c,
                        commentLikeRepository.countByCommentId(c.getId()),
                        commentLikeRepository.existsByCommentIdAndUserId(c.getId(), requestUserId)
                ))
                .toList();

        // nextCursor 계산
        Object nextCursor = null;
        if (dtos.size() == max) {
            CommentDto last = dtos.get(dtos.size() - 1);
            nextCursor = orderBy.equals("createdAt") ? last.createdAt() : last.likeCount();
        }

        return new PageResponse<>(
                dtos,
                nextCursor,
                true,
                dtos.size(),
                paged.size() > max,
                null
        );
    }

    @Override
    public CommentLikeDto likeComment(Long commentId, Long userId) {
        log.info("[likeComment] 댓글 좋아요 요청: commentId={}, userId={}", commentId, userId);
        return commentLikeService.likeComment(commentId, userId); // 위임
    }

    @Transactional
    public void softDeleteComment(Long id) {
        log.info("[CommentServiceImpl] 댓글 논리 삭제 시작: id:{}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException());

        comment.softDelete();
        commentRepository.save(comment);
        log.info("[CommentServiceImpl] 댓글 논리 삭제 성공: id:{}", id);

        publishUserActivityRemoveEvent(comment.getUser().getId(), id);
    }

    private void publishUserActivityRemoveEvent(Long userId, Long commentId) {
        UserActivityRemoveEvent event = new UserActivityRemoveEvent(
            userId,
            UserActivityType.COMMENT,
            null,
            commentId,
            null,
            null
        );
        eventPublisher.publishEvent(event);
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException());

        comment.updateContent(request.content()); // 이 메서드 Comment 엔티티에 있어야 함

        return CommentDto.fromEntity(comment, 0L, false); // likeCount, likedByMe는 기본값으로 처리
    }

    @Override
    @Transactional
    public void hardDelete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException());

        if (!comment.isDeleted()) {
            throw new CommentNotSoftDeletedException();
        }
        commentRepository.delete(comment);
    }

    private int compareInstant(Instant a, Instant b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }
}

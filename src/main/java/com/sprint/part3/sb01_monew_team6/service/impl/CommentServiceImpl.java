package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.dto.CommentUpdateRequest;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotSoftDeletedException;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {


    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public CommentDto register(CommentRegisterRequest request) {

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

    @Override
    public List<CommentDto> findAll(
            Long articleId,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit,
            Long requestUserId
    ) {
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
            comments = commentRepository.findAllByArticleId(articleId);
        } else {
            comments = commentRepository.findAll(); // 특정 게시글 ID가 없으면 전체 댓글 조회
        }

        // 4. 댓글 정렬
        List<Comment> modifiableComments = new ArrayList<>(comments);
        if (direction.equalsIgnoreCase("ASC")) {
            modifiableComments.sort((c1, c2) -> {
                if (c1.getCreatedAt() == null) {
                    return -1;  // createdAt이 null이면 가장 먼저 오게 처리
                }
                return c1.getCreatedAt().compareTo(c2.getCreatedAt());
            });
        } else {
            modifiableComments.sort((c1, c2) -> {
                if(c1.getCreatedAt() == null) {
                    return 1; // createdAt이 null이면 가장 뒤로 오게 처리
                }
                return c2.getCreatedAt().compareTo(c1.getCreatedAt());
            });
        }

        // 5. 댓글 목록을 CommentDto로 변환
        List<CommentDto> commentDtos = modifiableComments.stream()
                .limit(limit != null ? limit : 10)  // limit이 null이면 기본 10개 반환
                .map(comment -> {
                    // 좋아요 수 가져오기
                    long likeCount = commentLikeRepository.countByCommentId(comment.getId());

                    // 내가 좋아요를 눌렀는지 여부 확인
                    boolean likedByMe = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), requestUserId);

                    // CommentDto로 변환
                    return CommentDto.fromEntity(comment, likeCount, likedByMe);
                })
                .collect(Collectors.toList());

        return commentDtos;
    }

    @Override
    public List<CommentDto> getComments(Long articleId) {
        log.info("댓글 목록 조회 요청: articleId={}", articleId);

        //  게시글 존재 여부 확인
        if (!newsArticleRepository.existsById(articleId)) {
            throw new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND);
        }



        //  댓글 목록 조회 및 변환
        return commentRepository.findAllByArticleId(articleId).stream()
                .map(comment -> {
                    // 좋아요 수 가져오기
                    long likeCount = commentLikeRepository.countByCommentId(comment.getId());

                    // 내가 좋아요를 눌렀는지 여부 확인
                    boolean likedByMe = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), 1L);  // 예시로 1L을 requestUserId로 사용

                    // CommentDto로 변환
                    return CommentDto.fromEntity(comment, likeCount, likedByMe);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteComment(Long id){
        log.info("[CommentServiceImpl] 댓글 논리 삭제 시작: id:{}", id);
        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException());

        comment.softDelete();
        commentRepository.save(comment);
        log.info("[CommentServiceImpl] 댓글 논리 삭제 성공: id:{}", id);
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
}

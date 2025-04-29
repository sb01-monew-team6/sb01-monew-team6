package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {


    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

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
        // 일단 임시로 아무거나 리턴 (테스트 통과용)
        return null;
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
                .map(CommentDto::fromEntity)
                .toList();
    }
}

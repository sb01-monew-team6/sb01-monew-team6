package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.Source;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.UserActivityType;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.mapper.news.ArticleViewMapper;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.ArticleViewService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleViewServiceImpl implements ArticleViewService {
  private final NewsArticleRepository newsArticleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ArticleViewMapper articleViewMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public ArticleViewDto viewArticle(Long articleId, Long userId){
    //기사 및 유저가 있는지 확인
    NewsArticle article = newsArticleRepository.findById(articleId)
        .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NewsException(ErrorCode.NEWS_NOT_USER_FOUND_EXCEPTION,Instant.now(),HttpStatus.NOT_FOUND));

    //조회 기록 처리
    ArticleView view;
    if (articleViewRepository.existsByArticleIdAndUserId(articleId, userId)) {
      // 중복인 경우: 기존 기록만 조회
      view = articleViewRepository.findByArticleIdAndUserId(articleId, userId).orElseThrow(() -> new NewsException(
          ErrorCode.ARTICLE_VIEW_NOT_FOUND_EXCEPTION,
          Instant.now(),
          HttpStatus.INTERNAL_SERVER_ERROR));
    } else {
      // 처음 보는 경우에만 저장
      ArticleView newView = ArticleView.builder()
          .article(article)
          .user(user)
          .articleViewDate(Instant.now())
          .build();
      view = articleViewRepository.save(newView);
      publishUserActivityEvent(user, article, view);
    }

    //count 집계
    long commentCount = commentRepository.countByArticleIdAndIsDeletedFalse(articleId);
    long viewCount = articleViewRepository.countByArticleId(articleId);

    //dto 변환
    return articleViewMapper.toDto(view, commentCount, viewCount);
  }
  // 출처 목록 조회
  @Override
  public List<String> getSources(){
    // enum 값들을 문자열로 변환하여 반환
    return Arrays.stream(Source.values()).map(Source::name).toList();
  }

  private void publishUserActivityEvent(User user, NewsArticle article, ArticleView view) {
    UserActivityAddEvent event = new UserActivityAddEvent(
        user.getId(),
        UserActivityType.ARTICLE_VIEW,
        null,
        null,
        null,
        new ArticleViewHistoryDto(
            user.getId(),
            article.getId(),
            article.getSource(),
            article.getSourceUrl(),
            article.getArticleTitle(),
            LocalDateTime.ofInstant(article.getArticlePublishedDate(), ZoneOffset.UTC),
            article.getArticleSummary(),
            (long)article.getComments().size(),
            (long)article.getArticleViews().size(),
            view.getArticleViewDate()
        )
    );
    eventPublisher.publishEvent(event);
  }
}

package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.mapper.news.ArticleViewMapper;
import com.sprint.part3.sb01_monew_team6.repository.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
  private final NewsArticleRepository newsArticleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ArticleViewMapper articleViewMapper;

  public ArticleViewDto viewArticle(Long articleId, Long userId){
    //기사 및 유저가 있는지 확인
    NewsArticle article = newsArticleRepository.findById(articleId)
        .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NewsException(ErrorCode.NEWS_NOT_USER_FOUND_EXCEPTION,Instant.now(),HttpStatus.NOT_FOUND));
    // TODO: 나중에 예외 수정

    //조회 기록 저장
    Optional<ArticleView> existingView = articleViewRepository.findByArticleIdAndUserId(articleId, userId);

    if(existingView.isEmpty()) {//새로 저장
      ArticleView newView = new ArticleView(article, user, Instant.now());
      articleViewRepository.save(newView);
    }else{//중복
      ArticleView existing = existingView.get();
      existing.setArticleViewDate(Instant.now());
      articleViewRepository.save(existing);
    }

    //count 집계
    long commentCount = commentRepository.countByArticleId(articleId);
    long viewCount = articleViewRepository.countByArticleId(articleId);

    //dto 변환
    return articleViewMapper.toDto(existingView.orElse(null));
  }
}

package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import java.time.Instant;
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

  public ArticleViewDto viewArticle(Long articleId, Long userId){
    //기사 및 유저가 있는지 확인
    NewsArticle article = newsArticleRepository.findById(articleId)
        .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NewsException(ErrorCode.NEWS_NOT_USER_FOUND_EXCEPTION,Instant.now(),HttpStatus.NOT_FOUND)); //추후 UserException으로 변경

    //조회 기록 저장
    ArticleView view = new ArticleView(article,user,Instant.now());
    ArticleView savedView = articleViewRepository.save(view);

    //count 집계
    long commentCount = commentRepository.countByArticleId(articleId);
    long viewCount = articleViewRepository.countByArticleId(articleId);

    //dto 변환
    ArticleViewDto articleViewDto = new ArticleViewDto(
        savedView.getId(),
        savedView.getUser().getId(),
        savedView.getCreateAt(),
        savedView.getArticle().getId(),
        savedView.getArticle().getSource(),
        savedView.getArticle().getSourceUrl(),
        savedView.getArticle().getArticleTitle(),
        savedView.getArticle().getArticlePublishedDate().toString(),
        savedView.getArticle().getArticleSummary(),
        commentCount,
        viewCount
    );
    return articleViewDto;
  }

}

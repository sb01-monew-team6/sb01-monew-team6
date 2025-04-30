package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl {
  private final NewsArticleRepository newsArticleRepository;
  private final CommentRepository commentRepository;

//  public CursorPageResponseArticleDto searchArticles(CursorPageRequestArticleDto request) {
//    CursorPageRequestArticleDto requestDto = new CursorPageRequestArticleDto(
//        request.keyword(),request.interestId(),request.sourceIn(),
//        request.publishDateFrom(), request.publishDateTo(),request.orderBy(),
//        request.direction(),request.cursor(),request.after(),request.limit()
//    );
//  }
}

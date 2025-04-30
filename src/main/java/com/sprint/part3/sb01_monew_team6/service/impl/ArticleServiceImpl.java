package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageResponseArticleDto;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.ArticleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
  private final NewsArticleRepository newsArticleRepository;
  private final CommentRepository commentRepository;

  @Transactional(readOnly=true)
  @Override
  public CursorPageResponseArticleDto searchArticles(CursorPageRequestArticleDto request) {
    return CursorPageResponseArticleDto.toDto(
        List.of(), null, null,
        request.limit(), 0L, false
    );
  }
}

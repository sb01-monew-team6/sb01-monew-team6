package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageResponseArticleDto;

public interface ArticleService {
  CursorPageResponseArticleDto searchArticles(CursorPageRequestArticleDto request);
}

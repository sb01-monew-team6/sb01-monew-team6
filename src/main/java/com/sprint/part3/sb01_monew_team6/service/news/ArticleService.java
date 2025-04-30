package com.sprint.part3.sb01_monew_team6.service.news;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;

public interface ArticleService {
  PageResponse<ArticleDto> searchArticles(CursorPageRequestArticleDto request);
}

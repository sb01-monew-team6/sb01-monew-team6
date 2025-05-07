package com.sprint.part3.sb01_monew_team6.service.news;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;

public interface ArticleViewService {
  ArticleViewDto viewArticle(Long articleId, Long userId);
}

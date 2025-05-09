package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import java.util.List;

public interface ArticleViewService {
  ArticleViewDto viewArticle(Long articleId, Long userId);
  List<String> getSources();
}
package com.sprint.part3.sb01_monew_team6.service.news;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleRestoreResultDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ArticleService {
  PageResponse<ArticleDto> searchArticles(CursorPageRequestArticleDto request);
  List<ArticleRestoreResultDto> restore(LocalDate from, LocalDate to) throws IOException;
  void backup(LocalDate date) throws IOException;
  void deleteArticle(Long articleId);
  void hardDeleteArticle(Long articleId);
}

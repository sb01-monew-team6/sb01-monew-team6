package com.sprint.part3.sb01_monew_team6.repository.news;

import com.querydsl.core.types.OrderSpecifier;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.time.Instant;
import java.util.List;

public interface NewsArticleRepositoryCustom {
  List<NewsArticle> searchArticles(
      CursorPageRequestArticleDto request, // 검색 조건
      OrderSpecifier<?> orderSpec, // 정렬 스펙
      Long cursor,
      Instant after,
      int limit
  );
}

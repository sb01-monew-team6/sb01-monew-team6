package com.sprint.part3.sb01_monew_team6.repository.news;

import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticle.newsArticle;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsArticleRepositoryImpl implements NewsArticleRepositoryCustom {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<NewsArticle> searchArticles(CursorPageRequestArticleDto request,
      OrderSpecifier<?> orderSpec, Long cursor, Instant after, int limit) {
    return jpaQueryFactory
        .selectFrom(newsArticle)
        .where(newsArticle.isDeleted.eq(false))
        .orderBy(newsArticle.articlePublishedDate.desc())
        .limit(limit)
        .fetch();
  }
}

package com.sprint.part3.sb01_monew_team6.dto.news;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.time.Instant;

public record ArticleDto(
    String id,
    String source,
    String sourceUrl,
    String title,
    String summary,
    Instant publishDate,
    long commentCount,
    long viewCount,
    boolean viewedByMe
) {
  //NewsArticle 엔티티와 통계값(commentCount, viewCount, viewedByMe) -> ArticleDto
  public static ArticleDto from(NewsArticle a, long comments, long views, boolean viewed) {
    return new ArticleDto(
        a.getId().toString(),
        a.getSource(),
        a.getSourceUrl(),
        a.getArticleTitle(),
        a.getArticleSummary(),
        a.getArticlePublishedDate(),
        comments,
        views,
        viewed
    );
  }
}
package com.sprint.part3.sb01_monew_team6.mapper.news;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {
  @Mapping(target = "id",                     source = "articleView.id")
  @Mapping(target = "viewedBy",               source = "articleView.user.id")
  @Mapping(target = "createdAt",              source = "articleView.articleViewDate")
  @Mapping(target = "articleId",              source = "articleView.article.id")
  @Mapping(target = "source",                 source = "articleView.article.source")
  @Mapping(target = "sourceUrl",              source = "articleView.article.sourceUrl")
  @Mapping(target = "articleTitle",           source = "articleView.article.articleTitle")
  @Mapping(target = "articlePublishedDate",   expression = "java(articleView.getArticle().getArticlePublishedDate().toString())")
  @Mapping(target = "articleSummary",         source = "articleView.article.articleSummary")
  @Mapping(target = "articleCommentCount",    source = "commentCount")
  @Mapping(target = "articleViewCount",     source = "viewCount")
  ArticleViewDto toDto(ArticleView articleView, long commentCount, long viewCount);
}

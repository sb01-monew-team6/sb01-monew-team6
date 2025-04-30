package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.service.impl.ArticleViewImplService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{articleId}/article-views")
@RequiredArgsConstructor
public class ArticleViewController {

  private final ArticleViewImplService articleViewImplService;

  @PostMapping
  public ArticleViewDto viewArticle(
      @PathVariable Long articleId, @RequestParam("Monew-Request-User-ID") Long userId){
    return articleViewImplService.viewArticle(articleId, userId);
  }
}

package com.sprint.part3.sb01_monew_team6.controller;

import static org.springframework.http.HttpStatus.OK;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.service.impl.ArticleViewServiceImpl;
import javax.xml.transform.OutputKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping({
//    "/api/articles/{articleId}/article-views",
//    "/articles/{articleId}/article-views",
//    "/articles/articles/{articleId}/article-views"
//    "/api/articles"
//})
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleViewController {

  private final ArticleViewServiceImpl articleViewImplService;

  @PostMapping("/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> viewArticle(
      @PathVariable Long articleId, @RequestHeader("Monew-Request-User-ID") Long userId){
    ArticleViewDto articleView = articleViewImplService.viewArticle(articleId, userId);
    return ResponseEntity.status(OK).body(articleView);
  }

}

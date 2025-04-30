package com.sprint.part3.sb01_monew_team6.controller.news;

import static org.springframework.http.HttpStatus.OK;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.service.news.ArticleService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@Validated
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<PageResponse<ArticleDto>> archArticles(
      @RequestHeader("Monew-Request-User-ID")
      Long userId,
      @RequestParam(name = "keyword", required = false)
      String keyword,
      @RequestParam(name = "interestId", required = false)
      Long interestId,
      @RequestParam(name = "sourceIn", required = false)
      List<String> sourceIn,
      @RequestParam(name = "publishDateFrom", required = false)
      Instant publishDateFrom,
      @RequestParam(name = "publishDateTo", required = false)
      Instant publishDateTo,
      @RequestParam(name = "orderBy", required = true)
      String orderBy,
      @RequestParam(name = "direction", required = true)
      String direction,
      @RequestParam(name = "cursor", required = false)
      String cursor,
      @RequestParam(name = "after", required = false)
      Instant after,
      @RequestParam(name = "limit", required = true)
      int limit
  ) {
    CursorPageRequestArticleDto request = new CursorPageRequestArticleDto(
        userId, keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit
    );
    PageResponse<ArticleDto> article = articleService.searchArticles(request);
    return ResponseEntity.status(OK)
        .body(article);
  }
}

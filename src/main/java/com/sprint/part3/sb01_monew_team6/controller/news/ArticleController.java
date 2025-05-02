package com.sprint.part3.sb01_monew_team6.controller.news;

import static org.springframework.http.HttpStatus.OK;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleRestoreResultDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.service.news.ArticleService;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  //백업 복구
  @GetMapping("/restore")
  public List<ArticleRestoreResultDto> restore(
      @RequestParam("from")
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime from,

      @RequestParam("to")
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime to
  ) throws IOException {
    return articleService.restore(from.toLocalDate(), to.toLocalDate());
  }

  //논리 삭제
  @DeleteMapping("/{articleId}")
  public ResponseEntity<Void> delete(@PathVariable Long articleId) {
    articleService.deleteArticle(articleId);
    return ResponseEntity.noContent().build();
  }

  //물리 삭제
  @DeleteMapping("/{articleId}/hard")
  public ResponseEntity<Void> deleteHard(@PathVariable Long articleId){
    articleService.hardDeleteArticle(articleId);
    return ResponseEntity.noContent().build();
  }

}

package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CollectResponse;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.service.impl.NewsCollectionServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/collect/news")
@RequiredArgsConstructor
public class NewsCollectionController {
  private final NewsCollectionServiceImpl newsCollectionImplService;

  @GetMapping
  public ResponseEntity<Void> getCollectNews() throws InterruptedException {
    newsCollectionImplService.collectAndSave();
    return ResponseEntity.ok().build();
  }


  @PostMapping
  public ResponseEntity<?> collectNews() throws InterruptedException {
    // 뉴스 수집 및 저장
    Optional<List<NewsArticle>> collectedNews = newsCollectionImplService.collectAndSave();
    List<NewsArticle> saved = collectedNews.orElse(List.of());

    // 뉴스가 없으면 204 No Content 반환
    if (collectedNews.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    // 뉴스가 있으면 202 Accepted 상태 코드와 함께 메시지 반환
    List<ArticleDto> articleDtoList = saved
        .stream()
        .map(article->ArticleDto.from(article,0L,0L,false))
        .collect(Collectors.toList());

    CollectResponse body = new CollectResponse("배치 작업이 실행되었습니다.", articleDtoList.size(), articleDtoList);
    return ResponseEntity.accepted().body(body);
  }

}

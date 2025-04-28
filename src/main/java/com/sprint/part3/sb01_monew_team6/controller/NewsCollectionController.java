package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/collect")
@RequiredArgsConstructor
public class NewsCollectionController {
  private final NewsCollectionService newsCollectionService;

  @GetMapping("/news")
  public ResponseEntity<Void> getCollectNews() {
    newsCollectionService.collectAndSave();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/news")
  public ResponseEntity<?> collectNews() {
    newsCollectionService.collectAndSave();
    CollectResponse body = new CollectResponse("Batch job triggered");
    return ResponseEntity.accepted().body(body);
  }

  public static class CollectResponse {
    private final String message;
    public CollectResponse(String message) { this.message = message; }
    public String getMessage() { return message; }
  }
}

package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/collect")
@RequiredArgsConstructor
public class NewsCollectionController {
  private final NewsCollectionService newsCollectionService;

  @GetMapping("/news")
  public void collectNews() {
    newsCollectionService.collectAndSave();
  }
}

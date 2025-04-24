package com.sprint.part3.sb01_monew_team6.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class NewsArticleRepositoryTest {
  @Autowired
  private NewsArticleRepository newsArticleRepository;

  @Test
  @DisplayName("저장된 URL이 있으면 true 반환")
  void whenUrlExist_thenTrue() {
    //given
    NewsArticle article = NewsArticle.builder()
        .source("Naver")
        .sourceUrl("https://test.api.com")
        .articleTitle("test")
        .articlePublishedDate(Instant.now())
        .articleSummary("test")
        .build();

    newsArticleRepository.save(article);

    //when
    boolean exists = newsArticleRepository.existsBySourceUrl("https://test.api.com");

    //then
    assertThat(exists).isTrue();
  }
}

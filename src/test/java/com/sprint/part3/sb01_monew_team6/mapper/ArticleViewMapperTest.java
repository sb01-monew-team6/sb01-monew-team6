package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.mapper.news.ArticleViewMapper;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

public class ArticleViewMapperTest {
  private final ArticleViewMapper mapper = Mappers.getMapper(ArticleViewMapper.class);

  @Test
  @DisplayName("ArticleView → ArticleViewDto 매핑이 정확해야 한다")
  void toDto_mapsCorrectly() {
    // given
    Instant now = Instant.parse("2025-04-29T09:00:00Z");

    User user = User.builder()
        .email("tester@example.com")
        .nickname("tester")
        .password("password123")
        .build();
    ReflectionTestUtils.setField(user, "id", 1L);

    NewsArticle article = NewsArticle.builder()
        .source("Naver")
        .sourceUrl("https://naver.com/article")
        .articleTitle("테스트 기사")
        .articlePublishedDate(now)
        .articleSummary("요약입니다")
        .build();
    ReflectionTestUtils.setField(article, "id", 10L);

    ArticleView articleView = ArticleView.builder()
        .id(100L)
        .user(user)
        .article(article)
        .articleViewDate(now)
        .build();

    long commentCount = 5L;
    long viewCount = 123L;

    // when
    ArticleViewDto dto = mapper.toDto(articleView, commentCount, viewCount);

    // then
    assertThat(dto.id()).isEqualTo(100L);
    assertThat(dto.viewedBy()).isEqualTo(1L);
    assertThat(dto.createdAt()).isEqualTo(now);
    assertThat(dto.articleId()).isEqualTo(10L);
    assertThat(dto.source()).isEqualTo("Naver");
    assertThat(dto.sourceUrl()).isEqualTo("https://naver.com/article");
    assertThat(dto.articleTitle()).isEqualTo("테스트 기사");
    assertThat(dto.articlePublishedDate()).isEqualTo("2025-04-29T09:00:00Z");
    assertThat(dto.articleSummary()).isEqualTo("요약입니다");
    assertThat(dto.articleCommentCount()).isEqualTo(5L);
    assertThat(dto.articleViewCount()).isEqualTo(123L);
  }
}

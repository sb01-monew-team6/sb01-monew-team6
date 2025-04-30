package com.sprint.part3.sb01_monew_team6.repository.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import com.sprint.part3.sb01_monew_team6.config.TestDataJpaConfig;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestDataJpaConfig.class)
@AutoConfigureTestDatabase(replace = NONE)
public class NewsArticleRepositoryImplTest {
  @Autowired
  private EntityManager em;

  @Autowired
  private NewsArticleRepository newsArticleRepository;

  private NewsArticle a, b, c;

  @BeforeEach
  void setUp() {
    // publishDate 순서: a < b < c
    a = NewsArticle.builder()
        .articleTitle("A 기사")
        .articleSummary("요약A")
        .articlePublishedDate(Instant.parse("2025-01-01T00:00:00Z"))
        .source("S1")
        .sourceUrl("http://a")
        .build();
    b = NewsArticle.builder()
        .articleTitle("B 기사")
        .articleSummary("요약B")
        .articlePublishedDate(Instant.parse("2025-02-01T00:00:00Z"))
        .source("S1")
        .sourceUrl("http://b")
        .build();
    c = NewsArticle.builder()
        .articleTitle("C 기사")
        .articleSummary("요약C")
        .articlePublishedDate(Instant.parse("2025-03-01T00:00:00Z"))
        .source("S2")
        .sourceUrl("http://c")
        .build();


    em.persist(a);
    em.persist(b);
    em.persist(c);
    em.flush();
    em.clear();
  }
  @Test
  @DisplayName("검색어 없이 주어진 조건(DESC,limit=2)면 b,c 반환")
  void search_noKeyword_latestDesc_limit2(){
    //given
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto
        .builder()
        .orderBy("publishDate")
        .direction("DESC")
        .limit(2)
        .build();

    //when
    List<NewsArticle> result = newsArticleRepository.searchArticles(request,null,null,null,2);

    //then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("C 기사");
    assertThat(result.get(1).getArticleTitle()).isEqualTo("B 기사");
  }

  @Test
  @DisplayName("검색어 없이 주어진 조건(ASC, limit=2)면 오래된 2개(a, b) 반환")
  void search_noKeyword_asc_limit2() {
    // given
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("ASC")
        .limit(2)
        .build();

    // when
    List<NewsArticle> result = newsArticleRepository.searchArticles(req, null, null, null, 2);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("A 기사");
    assertThat(result.get(1).getArticleTitle()).isEqualTo("B 기사");
  }

  @Test
  @DisplayName("키워드 ‘C’ 로 검색하면 C 기사만 반환")
  void search_withKeyword_onlyC() {
    // given
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .keyword("C")
        .limit(10)
        .build();

    // when
    List<NewsArticle> result = newsArticleRepository.searchArticles(req, null, null, null, 10);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("C 기사");
  }
}

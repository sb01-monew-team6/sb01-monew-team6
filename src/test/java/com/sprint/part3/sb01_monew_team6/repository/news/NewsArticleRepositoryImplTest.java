package com.sprint.part3.sb01_monew_team6.repository.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import com.sprint.part3.sb01_monew_team6.config.TestDataJpaConfig;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticleInterest;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
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
  private NewsArticleRepository newsArticleRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private NewsArticleInterestRepository newsArticleInterestRepository;

  private NewsArticle a1,a2,a3;
  private Interest iSport;

  @BeforeEach
  void setUp() {
    // publishDate 순서: a < b < c
    a1 = newsArticleRepository.save(NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("u1")
        .articleTitle("Java")
        .articlePublishedDate(Instant.parse("2025-04-01T00:00:00Z"))
        .articleSummary("s1")
        .build()
    );
    a2 = newsArticleRepository.save(NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("u2")
        .articleTitle("Spring")
        .articlePublishedDate(Instant.parse("2025-04-02T00:00:00Z"))
        .articleSummary("s2")
        .build()
    );
    a3 = newsArticleRepository.save(NewsArticle.builder()
        .source("OTHER")
        .sourceUrl("u3")
        .articleTitle("Hibernate")
        .articlePublishedDate(Instant.parse("2025-04-03T00:00:00Z"))
        .articleSummary("s3")
        .build()
    );

    iSport = interestRepository.save(
        Interest.builder()
            .name("sports")
            .keywords(List.of("ball"))
            .build()
    );

    newsArticleInterestRepository.save(new NewsArticleInterest(a2, iSport));
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
    assertThat(result.get(0).getArticleTitle()).isEqualTo("Hibernate");
    assertThat(result.get(1).getArticleTitle()).isEqualTo("Spring");
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
    assertThat(result.get(0).getArticleTitle()).isEqualTo("Java");
    assertThat(result.get(1).getArticleTitle()).isEqualTo("Spring");
  }

  @Test
  @DisplayName("키워드 ‘H’ 로 검색하면 Hibernate 기사만 반환")
  void search_withKeyword_onlyC() {
    // given
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .keyword("H")
        .limit(10)
        .build();

    // when
    List<NewsArticle> result = newsArticleRepository.searchArticles(req, null, null, null, 10);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("Hibernate");
  }
  @Test @DisplayName("keyword 가 Spring 일 떄 Spring 기사만 반환")
  void seartch_withKeyword_onlySpring() {
    // Given
    CursorPageRequestArticleDto req = new CursorPageRequestArticleDto(
        "Spring",null,null,null,null,"publishDate","DESC",null,null,5
    );
    // When
    List<NewsArticle> res = newsArticleRepository.searchArticles(req,null,null,null,5);
    // Then
    assertThat(res).containsExactly(a2);
  }

  @Test @DisplayName("전체 범위 일 때 기사 수 3 반환")
  void nokeyword_totalCount_3() {
    // Given
    CursorPageRequestArticleDto req = new CursorPageRequestArticleDto(
        null,null,null,
        Instant.parse("2025-04-01T00:00:00Z"),
        Instant.parse("2025-04-03T23:59:59Z"),
        "publishDate","DESC",null,null,null
    );
    // When
    long total = newsArticleRepository.countArticles(req);
    // Then
    assertThat(total).isEqualTo(3L);
  }
}

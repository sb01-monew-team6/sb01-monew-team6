 package com.sprint.part3.sb01_monew_team6.repository.news;

 import static org.assertj.core.api.Assertions.assertThat;
 import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

 import com.sprint.part3.sb01_monew_team6.config.TestDataJpaConfig;
 import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
 import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
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
 public class NewsArticleRepositoryTest {
   @Autowired
   private NewsArticleRepository newsArticleRepository;

   private NewsArticle a, b, c;

   @BeforeEach
   void setUp() {
     // DB에 세 개의 기사 저장
     a = NewsArticle.builder()
         .source("NAVER")
         .sourceUrl("url1")
         .articleTitle("Spring 배우기")
         .articlePublishedDate(Instant.parse("2025-01-01T00:00:00Z"))
         .articleSummary("요약1")
         .build();

     b = NewsArticle.builder()
         .source("NAVER")
         .sourceUrl("url2")
         .articleTitle("Java 튜토리얼")
         .articlePublishedDate(Instant.parse("2025-02-01T00:00:00Z"))
         .articleSummary("요약2")
         .build();

     c = NewsArticle.builder()
         .source("OTHER")
         .sourceUrl("url3")
         .articleTitle("Kotlin 시작")
         .articlePublishedDate(Instant.parse("2025-03-01T00:00:00Z"))
         .articleSummary("요약3")
         .build();
     newsArticleRepository.saveAll(List.of(a, b, c));
   }

   @Test
   @DisplayName("저장된 URL이 있으면 true 반환")
   void whenUrlExist_thenTrue() {
     //given
     //newsArticleRepository.save(a);

     //when
     boolean exists = newsArticleRepository.existsBySourceUrl("url1");

     //then
     assertThat(exists).isTrue();
   }
   // 조회
   @Test
   @DisplayName("검색어 없이 주어진 조건(DESC,limit=2)면 b,c 반환")
   void search_noKeyword_latestDesc_limit2(){
     //given
     CursorPageRequestArticleDto request = CursorPageRequestArticleDto()
         .builder()
         .orderBy("publishDate")
         .direction("DESC")
         .limit(2)
         .build();

     //when
     List<NewsArticle> result = newsArticleRepository.searchArticles(request,null,null,null,2);

     //then- 아직 구현 전이므로 예외 또는 빈 리스트가 아닌 다른 결과여야 함
     assertThat(result).isEmpty();
   }
 }

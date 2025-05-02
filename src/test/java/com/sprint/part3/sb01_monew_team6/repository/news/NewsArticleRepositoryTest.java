 package com.sprint.part3.sb01_monew_team6.repository.news;

 import static org.assertj.core.api.Assertions.assertThat;
 import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

 import com.sprint.part3.sb01_monew_team6.config.JpaConfig;
 import com.sprint.part3.sb01_monew_team6.config.QueryDslConfig;
 import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
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
 @Import({JpaConfig.class, QueryDslConfig.class})
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
 }

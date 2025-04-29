 package com.sprint.part3.sb01_monew_team6.repository.news;

 import static org.assertj.core.api.Assertions.assertThat;

 import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
 import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
 import java.time.Instant;
 import org.junit.jupiter.api.DisplayName;
 import org.junit.jupiter.api.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
 import org.springframework.test.context.ActiveProfiles;

 @DataJpaTest
 @ActiveProfiles("test")
 public class NewsArticleRepositoryTest {
   @Autowired
   private NewsArticleRepository newsArticleRepository;

   @Test
   @DisplayName("저장된 URL이 있으면 true 반환")
   void whenUrlExist_thenTrue() {
     //given
     NewsArticle article = new NewsArticle();                     // 기본 생성자 사용
     article.setSource("Naver");                                  // 필드별 setter 호출
     article.setSourceUrl("https://test.api.com");
     article.setArticleTitle("test");
     article.setArticlePublishedDate(Instant.now());
     article.setArticleSummary("test");
     article.setDeleted(false);
     newsArticleRepository.save(article);

     //when
     boolean exists = newsArticleRepository.existsBySourceUrl("https://test.api.com");

     //then
     assertThat(exists).isTrue();
   }
 }

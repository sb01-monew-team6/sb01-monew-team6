package com.sprint.part3.sb01_monew_team6.repository.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import com.sprint.part3.sb01_monew_team6.config.JpaConfig;
import com.sprint.part3.sb01_monew_team6.config.QueryDslConfig;
import com.sprint.part3.sb01_monew_team6.config.TestDataJpaConfig;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import java.time.Instant;
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
public class ArticleViewRepositoryTest {
  @Autowired
  private ArticleViewRepository articleViewRepository;

  @Autowired
  private NewsArticleRepository newsArticleRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("조회 기록이 없으면 0을 반환")
  void noViews_returnZero() {
    //when
    long count = articleViewRepository.countByArticleId(1L);
    //then
    assertThat(count).isZero();
  }

  @Test
  @DisplayName("특정 기사의 조회수를 올바르게 반환")
  void countByArticleId_returnCorrectCount() {
    // given: User 저장
    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();
    userRepository.save(user);

    // given: NewsArticle 저장
    NewsArticle article = NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("https://example.com/some-article")
        .articleTitle("테스트 제목")
        .articlePublishedDate(Instant.parse("2025-04-27T00:00:00Z"))
        .articleSummary("테스트 요약입니다.")
        .build();
    newsArticleRepository.save(article);

    // given: 두 번의 ArticleView 저장
    ArticleView firstView = ArticleView.builder()
        .article(article)
        .user(user)
        .articleViewDate(Instant.now())
        .build();
    ArticleView secondView = ArticleView.builder()
        .article(article)
        .user(user)
        .articleViewDate(Instant.now())
        .build();
    articleViewRepository.save(firstView);
    articleViewRepository.save(secondView);

    // when: 카운트 조회
    long count = articleViewRepository.countByArticleId(article.getId());

    // then: 2번 저장했으니 2가 나와야 한다
    assertThat(count).isEqualTo(2);
  }
}

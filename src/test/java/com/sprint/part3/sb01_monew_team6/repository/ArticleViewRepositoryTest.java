package com.sprint.part3.sb01_monew_team6.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class ArticleViewRepositoryTest {
  @Autowired
  private ArticleViewRepository articleViewRepository;

  @Autowired
  private TestEntityManager em;

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
    //given
    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();
    em.persist(user);

    NewsArticle article = new NewsArticle();
    article.setSource("NAVER");
    article.setSourceUrl("https://example.com/some-article");
    article.setArticleTitle("테스트 제목");
    article.setArticlePublishedDate(Instant.parse("2025-04-27T00:00:00Z"));
    article.setArticleSummary("테스트 요약입니다.");
    em.persist(article);

    // 두 번 뷰를 기록
    em.persist(new ArticleView(article, user, Instant.now()));
    em.persist(new ArticleView(article, user, Instant.now()));

    em.flush();

    //when
    long count = articleViewRepository.countByArticleId(article.getId());

    //then
    assertThat(count).isEqualTo(2);
  }
}

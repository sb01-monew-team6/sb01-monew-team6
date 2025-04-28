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

@DataJpaTest
public class ArticleViewRepositoryTest {
  @Autowired
  private ArticleViewRepository articleViewRepository;

  @Autowired
  TestEntityManager em;

  @Test
  @DisplayName("특정 기사의 조회수를 올바르게 반환")
  void countByArticleId_returnCorrectCount() {
    //given
    NewsArticle article = new NewsArticle();
    em.persist(article);

    User user = new User();
    em.persist(user);

    em.persist(new ArticleView(article, user, Instant.now()));
    em.persist(new ArticleView(article, user, Instant.now()));

    em.flush();

    //when
    long count = articleViewRepository.countByArticleId(article.getId());

    //then
    assertThat(count).isEqualTo(2);
  }
}

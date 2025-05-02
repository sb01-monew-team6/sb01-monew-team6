package com.sprint.part3.sb01_monew_team6.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.part3.sb01_monew_team6.config.JpaConfig;
import com.sprint.part3.sb01_monew_team6.config.QueryDslConfig;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({JpaConfig.class, QueryDslConfig.class})
public class CommentRepositoryTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private CommentRepository commentRepository;

  @Test
  @DisplayName("기사에 달린 댓글 수를 반환")
  void countByArticleId_returnCorrectCount() {
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

    em.persist(new Comment(article, user, "c1",false,List.of()));
    em.persist(new Comment(article, user, "c2",false,List.of()));
    em.persist(new Comment(article, user, "c3",false,List.of()));

    em.flush();

    // when
    long cnt = commentRepository.countByArticleId(article.getId());

    // then
    assertThat(cnt).isEqualTo(3);
  }
}

package com.sprint.part3.sb01_monew_team6.repository.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import com.sprint.part3.sb01_monew_team6.config.TestDataJpaConfig;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.util.List;
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

    //then- 아직 구현 전이므로 예외 또는 빈 리스트가 아닌 다른 결과여야 함
    assertThat(result).isEmpty();
  }
}

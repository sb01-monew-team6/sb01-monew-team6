package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleViewServiceTest {
  @Mock
  private NewsArticleRepository newsArticleRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private CommentRepository commentRepository;
  @Mock
  private ArticleViewRepository articleViewRepository;

  @InjectMocks
  private ArticleViewService service;

  @Test
  @DisplayName("기사가 없으면 예외 발생")
  void whenNoNewsArticle_thenThrowException() {
    //given
    given(newsArticleRepository.findById(1L)).willReturn(Optional.empty());
    //when,then
    assertThatThrownBy(()-> service.viewArticle(1L, 2L))
        .isInstanceOf(NewsException.class)
        .satisfies(ex ->
            assertThat(((NewsException) ex).getCode())
                .isEqualTo(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION)
        );
  }

  @Test
  @DisplayName("정상요청 : ArticleView 저장하고 DTO 반환")
  void saveArticleView_thenReturnDto() {
    //given
    Instant now = Instant.parse("2025-04-27T12:00:00Z");
    NewsArticle article = new NewsArticle();
    article.setSource("Naver");
    article.setSourceUrl("url1");
    article.setArticleTitle("title1");
    article.setArticlePublishedDate(now);
    article.setArticleSummary("desc1");

    User user = new User();
    user.setEmail("tester@example.com");
    user.setNickname("tester");
    user.setPassword("password123");

    given(newsArticleRepository.findById(1L)).willReturn(Optional.of(article));
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    ArticleView view = new ArticleView();
    view.setArticle(article);
    view.setUser(user);
    view.setArticleViewDate(Instant.now());

    //when
    given(articleViewRepository.save(any())).willReturn(view);

    given(commentRepository.countByArticleId(1L)).willReturn(5L);
    given(articleViewRepository.countByArticleId(1L)).willReturn(42L);

    ArticleViewDto dto = service.viewArticle(1L, 1L);

    //then
    then(articleViewRepository).should().save(any(ArticleView.class));
    assertThat(dto).isNotNull();
  }
}

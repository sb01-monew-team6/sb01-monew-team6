package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleViewService {
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
    assertThatThrownBy(()->service.viewArticle(1L, UUID.randomUUID()))
        .isInstanceOf(NewsException.class)
        .satisfies(ex ->
            assertThat(((NewsException) ex).getCode())
                .isEqualTo(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION)
        );
  }
}

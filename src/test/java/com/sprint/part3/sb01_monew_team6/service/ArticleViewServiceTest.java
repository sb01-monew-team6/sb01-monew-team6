package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
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
import org.springframework.test.util.ReflectionTestUtils;

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
  @DisplayName("articleId,userId ID가 있으면 저장 후 DTO 반환")
  void givenId_thenSave_returnDTO(){
    //given
    Long articleId = 1L;
    Long userId = 2L;

    NewsArticle article = new NewsArticle();
    article.setSource("Naver");
    article.setSourceUrl("https://test.api.com");
    article.setArticleTitle("test");
    article.setArticlePublishedDate(Instant.parse("2025-04-27T12:00:00Z"));
    article.setArticleSummary("test");
    article.setDeleted(false);
    ReflectionTestUtils.setField(article, "id", articleId);


    User user = new User();
    user.setNickname("user");
    user.setEmail("email");
    user.setPassword("pwd");
    ReflectionTestUtils.setField(user, "id", userId);

    ArticleView view = new ArticleView(article, user, Instant.parse("2025-04-27T12:00:00Z"));

    given(newsArticleRepository.findById(articleId)).willReturn(Optional.of(article));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(articleViewRepository.save(any(ArticleView.class))).willReturn(view);
    given(commentRepository.countByArticleId(articleId)).willReturn(5L);
    given(articleViewRepository.countByArticleId(articleId)).willReturn(1L);

    // when
    ArticleViewDto dto = service.viewArticle(articleId, userId);

    // then
    assertAll("DTO 검증",
        () -> assertThat(dto.articleId()).isEqualTo(articleId),
        () -> assertThat(dto.viewedBy()).isEqualTo(userId),
        () -> assertThat(dto.articleCommentCount()).isEqualTo(5L),
        () -> assertThat(dto.articleViewCount()).isEqualTo(1L)
    );
  }
  @Test
  @DisplayName("articleId가 없을 경우 에러 발생")
  void articleIdNotExist_thenThrowException(){
    //then
    given(newsArticleRepository.findById(any(Long.class))).willReturn(Optional.empty());
    //then
    assertThatThrownBy(()->service.viewArticle(1L, 2L))
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("기사가 존재하지 않습니다.");
  }
  @Test
  @DisplayName("userId가 없을 경우 에러 발생")
  void userIdNotExist_thenThrowException(){
    //given
    Long articleId = 1L;

    NewsArticle article = new NewsArticle();
    article.setSource("Naver");
    article.setSourceUrl("https://test.api.com");
    article.setArticleTitle("test");
    article.setArticlePublishedDate(Instant.parse("2025-04-27T12:00:00Z"));
    article.setArticleSummary("test");
    article.setDeleted(false);
    ReflectionTestUtils.setField(article, "id", articleId);
    given(newsArticleRepository.findById(1L))
        .willReturn(Optional.of(article));
    //then
    given(userRepository.findById(any(Long.class))).willReturn(Optional.empty());
    //then
    assertThatThrownBy(()->service.viewArticle(1L, 2L))
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("유저가 존재하지 않습니다.");
  }

  @Test
  @DisplayName("기사 중복 조회 시 저장 없이 기존 집계만 반환")
  void articleView_duplicate(){
    //given
    Long articleId = 1L;
    Long userId = 2L;

    NewsArticle article = new NewsArticle();
    article.setSource("Naver");
    article.setSourceUrl("https://test.api.com");
    article.setArticleTitle("test");
    article.setArticlePublishedDate(Instant.parse("2025-04-27T12:00:00Z"));
    article.setArticleSummary("test");
    article.setDeleted(false);
    ReflectionTestUtils.setField(article, "id", articleId);
    given(newsArticleRepository.findById(articleId)).willReturn(Optional.of(article));

    User user = new User();
    user.setNickname("user");
    user.setEmail("email");
    user.setPassword("pwd");
    ReflectionTestUtils.setField(user, "id", userId);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    //when
    given(articleViewRepository.existsByArticleIdAndUserId(articleId, userId)).willReturn(true);
    given(commentRepository.countByArticleId(articleId)).willReturn(0L);
    given(articleViewRepository.countByArticleId(articleId)).willReturn(3L);

    ArticleViewDto dto = service.viewArticle(articleId, userId);

    //then
    assertThat(dto.articleViewCount()).isEqualTo(3L);
    then(articleViewRepository).should(never()).save(any(ArticleView.class));
  }

}

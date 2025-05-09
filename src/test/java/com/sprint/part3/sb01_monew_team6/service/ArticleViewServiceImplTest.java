package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.event.UserActivityAddEvent;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.mapper.news.ArticleViewMapper;
import com.sprint.part3.sb01_monew_team6.repository.news.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.ArticleViewServiceImpl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ArticleViewServiceImplTest {
  @Mock
  private NewsArticleRepository newsArticleRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private CommentRepository commentRepository;
  @Mock
  private ArticleViewRepository articleViewRepository;
  @Mock
  private ArticleViewMapper articleViewMapper;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private ArticleViewServiceImpl service;

  @Captor
  private ArgumentCaptor<UserActivityAddEvent> userActivityEventCaptor;

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


    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);

    ArticleView view = ArticleView.builder()
        .article(article)
        .user(user)
        .articleViewDate(Instant.now())
        .build();;
    ReflectionTestUtils.setField(view, "id", 10L);

    given(newsArticleRepository.findById(articleId)).willReturn(Optional.of(article));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(articleViewRepository.save(any(ArticleView.class))).willReturn(view);
    given(commentRepository.countByArticleIdAndIsDeletedFalse(articleId)).willReturn(5L);
    given(articleViewRepository.countByArticleId(articleId)).willReturn(1L);

    // Mapper가 호출될 때 기대 DTO를 반환하도록 정의
    ArticleViewDto expectedDto = ArticleViewDto.builder()
        .id(view.getId())
        .viewedBy(userId)
        .createdAt(view.getArticleViewDate())
        .articleId(articleId)
        .source(article.getSource())
        .sourceUrl(article.getSourceUrl())
        .articleTitle(article.getArticleTitle())
        .articlePublishedDate(article.getArticlePublishedDate().toString())
        .articleSummary(article.getArticleSummary())
        .articleCommentCount(5L)
        .articleViewCount(1L)
        .build();
    given(articleViewMapper.toDto(view, 5L, 1L))
        .willReturn(expectedDto);

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

    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // 이미 조회했음을 확인하도록 stub
    given(articleViewRepository.existsByArticleIdAndUserId(articleId, userId))
        .willReturn(true);

    // 기존에 저장된 ArticleView 인스턴스 준비
    ArticleView existingView = ArticleView.builder()
        .article(article)
        .user(user)
        .articleViewDate(Instant.now())
        .build();
    ReflectionTestUtils.setField(existingView, "id", 100L);
    given(articleViewRepository.findByArticleIdAndUserId(articleId, userId))
        .willReturn(Optional.of(existingView));

    given(commentRepository.countByArticleIdAndIsDeletedFalse(articleId)).willReturn(0L);
    given(articleViewRepository.countByArticleId(articleId)).willReturn(3L);

    ArticleViewDto expectedDto = ArticleViewDto.builder()
        .id(null)  // 서비스가 중복 조회일 땐 id 없이 처리한다면 null
        .viewedBy(userId)
        .createdAt(null)
        .articleId(articleId)
        .source(article.getSource())
        .sourceUrl(article.getSourceUrl())
        .articleTitle(article.getArticleTitle())
        .articlePublishedDate(article.getArticlePublishedDate().toString())
        .articleSummary(article.getArticleSummary())
        .articleCommentCount(0L)
        .articleViewCount(3L)
        .build();
    given(articleViewMapper.toDto(existingView, 0L, 3L))
        .willReturn(expectedDto);

    //when
    ArticleViewDto dto = service.viewArticle(articleId, userId);

    //then
    assertThat(dto).isEqualTo(expectedDto);
    then(articleViewRepository).should(never()).save(any(ArticleView.class));
  }

  @Test
  @DisplayName("기사 조회 정상 호출 시 유저 활동 내역 기사 조회 추가")
  void addArticleView(){
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

    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);

    ArticleView view = ArticleView.builder()
        .article(article)
        .user(user)
        .articleViewDate(Instant.now())
        .build();
    ReflectionTestUtils.setField(view, "id", 10L);

    given(newsArticleRepository.findById(articleId)).willReturn(Optional.of(article));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(articleViewRepository.save(any(ArticleView.class))).willReturn(view);
    given(commentRepository.countByArticleIdAndIsDeletedFalse(articleId)).willReturn(5L);
    given(articleViewRepository.countByArticleId(articleId)).willReturn(1L);

    ArticleViewDto expectedDto = ArticleViewDto.builder()
        .id(view.getId())
        .viewedBy(userId)
        .createdAt(view.getArticleViewDate())
        .articleId(articleId)
        .source(article.getSource())
        .sourceUrl(article.getSourceUrl())
        .articleTitle(article.getArticleTitle())
        .articlePublishedDate(article.getArticlePublishedDate().toString())
        .articleSummary(article.getArticleSummary())
        .articleCommentCount(5L)
        .articleViewCount(1L)
        .build();
    given(articleViewMapper.toDto(view, 5L, 1L))
        .willReturn(expectedDto);

    // when
    service.viewArticle(articleId, userId);

    // then
    verify(eventPublisher, times(1)).publishEvent(userActivityEventCaptor.capture());
    UserActivityAddEvent published = userActivityEventCaptor.getValue();
    assertThat(published.userId()).isEqualTo(userId);
    assertThat(published.articleView().articleId()).isEqualTo(articleId);
  }

  @DisplayName("getSources() 호출 시 모든 enum 값 반환")
  @Test
  void getSources_returnsAllEnumNames() {
    // when
    List<String> sources = service.getSources();

    // then
    assertThat(sources).containsExactly(
        "NAVER",
        "JTBC",
        "HANKYUNG",
        "YEONHAP",
        "YEONHAP_POLITICS"
    );
  }
}

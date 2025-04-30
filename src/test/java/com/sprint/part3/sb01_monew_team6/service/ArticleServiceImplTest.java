package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageResponseArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.ArticleServiceImpl;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceImplTest {
  @Mock
  NewsArticleRepository newsArticleRepository;
  @Mock
  CommentRepository commentRepository;
  @InjectMocks
  ArticleServiceImpl articleService;

  @Test
  @DisplayName("레포지토리가 비어있을 때, 검색을 실행하면 null 발생")
  void repositoryEmpty_thenNull(){
    //given
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .limit(3)
        .build();

    //when
    CursorPageResponseArticleDto response = articleService.searchArticles(request);

    //then
    assertThat(response.content()).isEmpty();
    assertThat(response.totalElements()).isEqualTo(0L);
    assertThat(response.hasNext()).isFalse();
  }

  @Test
  @DisplayName("limit=2 일 때 hasNext,totalElements 검증")
  void validation_thenHasNextTotalElements() {
    // given
    NewsArticle a1 = NewsArticle.builder()
        .articlePublishedDate(Instant.parse("2025-04-01T00:00:00Z"))
        .build();
    ReflectionTestUtils.setField(a1, "id", 1L);

    NewsArticle a2 = NewsArticle.builder()
        .articlePublishedDate(Instant.parse("2025-04-02T00:00:00Z"))
        .build();
    ReflectionTestUtils.setField(a2, "id", 2L);

    given(newsArticleRepository.searchArticles(any(), any(), any(), any(), eq(2)))
        .willReturn(List.of(a1, a2));
    given(newsArticleRepository.countArticles(any())).willReturn(3L);
    given(commentRepository.countByArticleId(anyLong())).willReturn(0L);

    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .limit(2)
        .build();

    // when
    CursorPageResponseArticleDto dto = articleService.searchArticles(request);

    // then
    assertThat(dto.hasNext()).isTrue();
    assertThat(dto.nextCursor()).isEqualTo("2");
    assertThat(dto.totalElements()).isEqualTo(3L);
  }
}

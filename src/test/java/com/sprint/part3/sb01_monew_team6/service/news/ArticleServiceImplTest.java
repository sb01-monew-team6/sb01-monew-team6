package com.sprint.part3.sb01_monew_team6.service.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.impl.ArticleServiceImpl;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceImplTest {
  @Mock
  NewsArticleRepository newsArticleRepository;
  @Mock
  CommentRepository commentRepository;
  @Mock
  PageResponseMapper pageResponseMapper;
  @InjectMocks
  ArticleServiceImpl articleService;

  @Test
  @DisplayName("조회 결과가 없으면 빈 페이지 반환")
  void noArticle_thenEmptyPage(){
    //given
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .userId(1L)
        .orderBy("id")
        .direction("DESC")
        .limit(5)
        .build();

    given(newsArticleRepository.searchArticles(
        any(CursorPageRequestArticleDto.class),
        any(),            // orderSpec
        any(),            // cursor
        any(),            // after
        anyInt()          // limit
    ))
        .willReturn(List.of());
    given(newsArticleRepository.countArticles(request)).willReturn(0L);

    // mapper stubbing: 빈 Slice → 빈 PageResponse 반환
    PageResponse<ArticleDto> emptyResp = new PageResponse<>(
    List.of(), null, null, 5, false, 0L);
    given(pageResponseMapper.fromSlice(any(Slice.class), any(), any(), anyLong())).willReturn(emptyResp);

    //when
    PageResponse<ArticleDto> page = articleService.searchArticles(request);

    //then
    assertThat(page.contents()).isEmpty();
    assertThat(page.hasNext()).isFalse();
    assertThat(page.totalElements()).isZero();
    assertThat(page.nextCursor()).isNull();
    assertThat(page.nextAfter()).isNull();
    assertThat(page.size()).isEqualTo(5);
  }

  @Test
  @DisplayName("조회 정상")
  void service_thenNormal(){
    //given
    CursorPageRequestArticleDto request = new CursorPageRequestArticleDto(
        1L, "스포츠", null, List.of("NAVER"), null, null, "publishDate", "DESC", null, null, 10
    );

    List<NewsArticle> articleList = List.of(
        NewsArticle.builder().articleTitle("축구").articlePublishedDate(Instant.now()).build()
    );
    ReflectionTestUtils.setField(articleList.get(0), "id", 1L);
    List<ArticleDto> dtoList = List.of(
        new ArticleDto(1L, "NAVER", "url", "제목", "요약", Instant.now(), 2L, 1L, false)
    );
    given(newsArticleRepository.searchArticles(any(), any(), any(), any(), anyInt())).willReturn(articleList);
    given(newsArticleRepository.countArticles(any())).willReturn(1L);
    given(commentRepository.countByArticleId(anyLong())).willReturn(2L);
    doReturn(new PageResponse<>(dtoList, "1", Instant.now(), 10, false, 1L))
        .when(pageResponseMapper)
        .fromSlice(any(org.springframework.data.domain.Slice.class),
            any(), any(), anyLong());

    //when
    PageResponse<ArticleDto> result = articleService.searchArticles(request);

    //then
    assertThat(result.contents()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1L);
    verify(newsArticleRepository).searchArticles(any(), any(), any(), any(), anyInt());
    verify(commentRepository).countByArticleId(1L);
    verify(pageResponseMapper).fromSlice(any(), any(), any(), anyLong());
  }

  @Test
  @DisplayName("커서 파라미터가 null 일 때 예외 없이 동작")
  void cursor_null_thenNormal() {
    //given
    CursorPageRequestArticleDto request = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null, "id", "ASC", null, null, 1
    );

    given(newsArticleRepository.searchArticles(any(), any(), any(), any(), anyInt()))
        .willReturn(List.of());
    given(newsArticleRepository.countArticles(any())).willReturn(0L);
    given(pageResponseMapper.fromSlice(any(), any(), any(), anyLong()))
        .willReturn(new PageResponse<>(List.of(), null, null, 1, false, 0L));

    //when
    PageResponse<ArticleDto> result = articleService.searchArticles(request);

    //then
    assertThat(result.contents()).isEmpty();
    assertThat(result.totalElements()).isZero();
  }
}

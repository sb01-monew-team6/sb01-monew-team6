package com.sprint.part3.sb01_monew_team6.service.news;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.type.TypeReference;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleRestoreResultDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.impl.ArticleServiceImpl;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceImplTest {
  @Mock
  NewsArticleRepository newsArticleRepository;
  @Mock
  CommentRepository commentRepository;
  @Mock
  PageResponseMapper pageResponseMapper;
  @Mock
  S3Client s3Client;
  @Mock
  ObjectMapper objectMapper;
  @InjectMocks
  ArticleServiceImpl articleService;

  private final String bucketName = "my-test-bucket";

  //목록 조회 : 페이지네이션
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

  //백업 복구
  @Test
  @DisplayName("restore(): DB에 없는 백업 기사만 복구하고 카운트 반환")
  void restore_addsMissingOnly() throws Exception {
    // given
    LocalDate date = LocalDate.of(2025, 5, 2);
    byte[] bytes = "[{\"id\":999}]".getBytes();

    GetObjectResponse dummyResp = GetObjectResponse.builder().build();
    ResponseBytes<GetObjectResponse> respBytes =
        ResponseBytes.fromByteArray(dummyResp, bytes);

    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(respBytes);

    when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
        .thenReturn(List.of(new NewsArticle()));
    when(newsArticleRepository.existsBySourceUrl(any())).thenReturn(false);
    when(newsArticleRepository.save(any(NewsArticle.class)))
        .thenAnswer(inv -> {
          NewsArticle a = inv.getArgument(0, NewsArticle.class);
          ReflectionTestUtils.setField(a, "id", 999L);
          return a;
        });

    // when
    List<ArticleRestoreResultDto> result = articleService.restore(date, date);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).restoredArticleCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("restore(): 이미 DB에 있으면 save 호출 없이 카운트 0 반환")
  void restore_skipsExisting() throws Exception {
    // given
    LocalDate date = LocalDate.of(2025, 5, 2);
    byte[] bytes = "[{\"sourceUrl\":\"u1\"}]".getBytes();
    GetObjectResponse dummyResp = GetObjectResponse.builder().build();
    ResponseBytes<GetObjectResponse> respBytes =
        ResponseBytes.fromByteArray(dummyResp, bytes);

    // S3에서 바이트 배열 받아오기
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(respBytes);

    // JSON → List<NewsArticle>
    NewsArticle backup = new NewsArticle();
    ReflectionTestUtils.setField(backup, "sourceUrl", "u1");
    when(objectMapper.readValue(eq(bytes), any(TypeReference.class)))
        .thenReturn(List.of(backup));

    // 이미 존재한다고 응답
    when(newsArticleRepository.existsBySourceUrl("u1")).thenReturn(true);

    // when
    List<ArticleRestoreResultDto> results = articleService.restore(date, date);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.get(0).restoredArticleCount()).isZero();
    verify(newsArticleRepository, never()).save(any(NewsArticle.class));
  }

  @Test
  @DisplayName("restore(): 다중 날짜 범위 처리")
  void restore_multipleDays() throws Exception {
    // given
    LocalDate from = LocalDate.of(2025, 5, 1);
    LocalDate to   = LocalDate.of(2025, 5, 2);

    // Day1 백업
    byte[] b1 = "[{\"sourceUrl\":\"u1\"}]".getBytes();
    GetObjectResponse r1 = GetObjectResponse.builder().build();
    ResponseBytes<GetObjectResponse> rb1 =
        ResponseBytes.fromByteArray(r1, b1);
    NewsArticle ba1 = new NewsArticle();
    ReflectionTestUtils.setField(ba1, "sourceUrl", "u1");

    // Day2 백업
    byte[] b2 = "[{\"sourceUrl\":\"u2\"}]".getBytes();
    GetObjectResponse r2 = GetObjectResponse.builder().build();
    ResponseBytes<GetObjectResponse> rb2 =
        ResponseBytes.fromByteArray(r2, b2);
    NewsArticle ba2 = new NewsArticle();
    ReflectionTestUtils.setField(ba2, "sourceUrl", "u2");

    // S3 호출 순서대로 두 번 리턴
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(rb1, rb2);

    // JSON → 객체 리스트 순서대로 리턴
    when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
        .thenReturn(List.of(ba1), List.of(ba2));

    // DB에 없다고 응답
    when(newsArticleRepository.existsBySourceUrl("u1")).thenReturn(false);
    when(newsArticleRepository.existsBySourceUrl("u2")).thenReturn(false);

    // save 시 id 부여
    when(newsArticleRepository.save(any(NewsArticle.class)))
        .thenAnswer(inv -> {
          NewsArticle a = inv.getArgument(0, NewsArticle.class);
          String url = (String) ReflectionTestUtils.getField(a, "sourceUrl");
          long id = url.equals("u1") ? 1L : 2L;
          ReflectionTestUtils.setField(a, "id", id);
          return a;
        });

    // when
    List<ArticleRestoreResultDto> results = articleService.restore(from, to);

    // then
    assertThat(results).hasSize(2);

    assertThat(results.get(0).restoreDate()).isEqualTo(from);
    assertThat(results.get(0).restoredArticleCount()).isEqualTo(1);
    assertThat(results.get(0).restoredArticleIds()).containsExactly(1L);

    assertThat(results.get(1).restoreDate()).isEqualTo(to);
    assertThat(results.get(1).restoredArticleCount()).isEqualTo(1);
    assertThat(results.get(1).restoredArticleIds()).containsExactly(2L);

    verify(s3Client, times(2)).getObjectAsBytes(any(GetObjectRequest.class));
    verify(newsArticleRepository, times(2)).save(any(NewsArticle.class));
  }
  //논리삭제
  @Test
  @DisplayName("ID가 없으면 예외 발생")
  void noId_thenException() {
    //given

    //when
    when(newsArticleRepository.findById(1L)).thenReturn(Optional.empty());

    //then
    assertThrows(NewsException.class,() -> articleService.deleteArticle(1L));
  }
  @Test
  @DisplayName("ID가 있으면 isDeleted=true ")
  void id_thenIsDeletedTrue() {
    //given
    NewsArticle a1 = NewsArticle.from(
        new ExternalNewsItem("Naver","url1","url1","title1",Instant.now(),"desc1"));
    ReflectionTestUtils.setField(a1, "id", 1L);
    when(newsArticleRepository.findById(1L)).thenReturn(Optional.of(a1));

    //when
    articleService.deleteArticle(1L);

    //then
    assertThat(a1.isDeleted()).isTrue();
    verify(newsArticleRepository).save(a1);
  }
}

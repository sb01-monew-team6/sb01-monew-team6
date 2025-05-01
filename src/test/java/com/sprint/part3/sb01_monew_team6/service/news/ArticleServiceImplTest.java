package com.sprint.part3.sb01_monew_team6.service.news;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.impl.ArticleServiceImpl;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
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
    LocalDate date = LocalDate.of(2025, 4, 30);
    byte[] jsonBytes = "[{\"sourceUrl\":\"url1\"}]".getBytes();

    // S3 응답 모킹
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(ResponseBytes.fromByteArray(
            GetObjectResponse.builder().build(), jsonBytes
        ));

    // JSON → 엔티티 변환 모킹
    NewsArticle dummy = NewsArticle.builder()
        .source("testSource")
        .sourceUrl("url1")
        .articleTitle("Test Title")
        .articlePublishedDate(Instant.parse("2025-04-30T00:00:00Z"))
        .articleSummary("Test Summary")
        .build();
    doReturn(List.of(dummy))
        .when(objectMapper)
        .readValue(any(byte[].class), any(TypeReference.class));

    // DB에 없다고 응답
    when(newsArticleRepository.existsBySourceUrl("url1")).thenReturn(false);
    when(newsArticleRepository.save(any(NewsArticle.class)))
        .thenAnswer(invocation -> {
          NewsArticle a = invocation.getArgument(0, NewsArticle.class);
          try {
            Field idField = NewsArticle.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(a, 100L);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return a;
        });

    // 실행
    List<ArticleRestoreResultDto> results = articleService.restore(date, date);

    // 검증
    assertThat(results).hasSize(1);
    ArticleRestoreResultDto dto = results.get(0);
    assertThat(dto.getDate()).isEqualTo(date);
    assertThat(dto.getRestoredArticleCount()).isEqualTo(1);
    assertThat(dto.getRestoredIds()).containsExactly(100L);

    // S3 Key 확인
    ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObjectAsBytes(captor.capture());
    assertThat(captor.getValue().key()).isEqualTo("backup/2025-04-30.json");
  }

  @Test
  @DisplayName("restore(): 이미 DB에 있으면 save 호출 없이 카운트 0 반환")
  void restore_skipsExisting() throws Exception {
    LocalDate date = LocalDate.of(2025, 4, 30);
    byte[] jsonBytes = "[{\"sourceUrl\":\"urlX\"}]".getBytes();

    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(ResponseBytes.fromByteArray(
            GetObjectResponse.builder().build(), jsonBytes
        ));
    doReturn(List.of(
        NewsArticle.builder()
            .source("src")
            .sourceUrl("urlX")
            .articleTitle("T")
            .articlePublishedDate(Instant.now())
            .articleSummary("S")
            .build()
    ))
        .when(objectMapper)
        .readValue(any(byte[].class), any(TypeReference.class));

    when(newsArticleRepository.existsBySourceUrl("urlX")).thenReturn(true);

    // 실행
    List<ArticleRestoreResultDto> results = articleService.restore(date, date);

    // 검증
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getRestoredArticleCount()).isZero();
    verify(newsArticleRepository, never()).save(any());
  }

  @Test
  @DisplayName("restore(): 다중 날짜 범위 처리")
  void restore_multipleDays() throws Exception {
    LocalDate from = LocalDate.of(2025, 4, 29);
    LocalDate to = LocalDate.of(2025, 4, 30);
    byte[] day1 = "[{\"sourceUrl\":\"a\"}]".getBytes();
    byte[] day2 = "[{\"sourceUrl\":\"b\"},{\"sourceUrl\":\"c\"}]".getBytes();

    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), day1))
        .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), day2));

    doReturn(List.of(
        NewsArticle.builder()
            .source("s")
            .sourceUrl("a")
            .articleTitle("A")
            .articlePublishedDate(Instant.now())
            .articleSummary("sum")
            .build()
    ))
        .when(objectMapper)
        .readValue(any(byte[].class), any(TypeReference.class));
    doReturn(List.of(
        NewsArticle.builder()
            .source("s")
            .sourceUrl("b")
            .articleTitle("B")
            .articlePublishedDate(Instant.now())
            .articleSummary("sum")
            .build(),
        NewsArticle.builder()
            .source("s")
            .sourceUrl("c")
            .articleTitle("C")
            .articlePublishedDate(Instant.now())
            .articleSummary("sum")
            .build()
    ))
        .when(objectMapper)
        .readValue(any(byte[].class), any(TypeReference.class));

    when(newsArticleRepository.existsBySourceUrl(anyString())).thenReturn(false);
    when(newsArticleRepository.save(any(NewsArticle.class)))
        .thenAnswer(invocation -> {
          NewsArticle a = invocation.getArgument(0, NewsArticle.class);
          try {
            Field idField = NewsArticle.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(a, 1L);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return a;
        });

    List<ArticleRestoreResultDto> results = articleService.restore(from, to);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getRestoredArticleCount()).isEqualTo(1);
    assertThat(results.get(1).getRestoredArticleCount()).isEqualTo(2);
    verify(newsArticleRepository, times(3)).save(any(NewsArticle.class));
  }

}

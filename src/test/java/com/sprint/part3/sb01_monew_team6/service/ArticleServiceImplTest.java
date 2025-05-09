package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.sprint.part3.sb01_monew_team6.repository.news.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.ArticleServiceImpl;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceImplTest {
  @Mock
  NewsArticleRepository newsArticleRepository;
  @Mock
  ArticleViewRepository articleViewRepository;
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

  @BeforeEach
  void init() {
    // 세터로 버킷 이름을 지정
    articleService.setBucketName("test-bucket");
  }

  //목록 조회 : 페이지네이션
  @Test
  @DisplayName("조회 결과가 없으면 빈 페이지 반환")
  void noArticle_thenEmptyPage(){
    // given: orderBy를 publishDate로 변경
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .userId(1L)
        .orderBy("publishDate")
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

    // mapper stub: 빈 Slice → 빈 PageResponse
    PageResponse<ArticleDto> emptyResp = new PageResponse<>(
        List.of(), null, null, 5, false, 0L);
    given(pageResponseMapper.fromSlice(any(Slice.class), any(), any(), anyLong()))
        .willReturn(emptyResp);

    // when
    PageResponse<ArticleDto> page = articleService.searchArticles(request);

    // then
    assertThat(page.contents()).isEmpty();
    assertThat(page.hasNext()).isFalse();
    assertThat(page.totalElements()).isZero();
    assertThat(page.nextCursor()).isNull();
    assertThat(page.nextAfter()).isNull();
    assertThat(page.size()).isEqualTo(5);
  }

  @Test
  @DisplayName("조회 정상")
  void service_thenNormal() {
    // given
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .userId(1L)
        .keyword("스포츠")
        .sourceIn(List.of("NAVER"))
        .orderBy("publishDate")
        .direction("DESC")
        .limit(10)
        .build();

    NewsArticle article = NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("url")
        .articleTitle("축구")
        .articlePublishedDate(Instant.parse("2025-05-09T00:00:00Z"))
        .articleSummary("요약")
        .build();
    ReflectionTestUtils.setField(article, "id", 1L);

    given(newsArticleRepository.searchArticles(
        any(CursorPageRequestArticleDto.class),
        any(), any(), any(), anyInt()))
        .willReturn(List.of(article));
    given(newsArticleRepository.countArticles(any()))
        .willReturn(1L);

    given(commentRepository.countByArticleId(1L))
        .willReturn(5L);
    given(commentRepository.countByArticleIdAndIsDeletedFalse(1L))
        .willReturn(3L);

    given(articleViewRepository.countByArticleId(1L))
        .willReturn(7L);
    given(articleViewRepository.existsByArticleIdAndUserId(1L, 1L))
        .willReturn(true);

    PageResponse<ArticleDto> stubResponse = new PageResponse<>(
        List.of(new ArticleDto(
            1L,
            "NAVER",
            "url",
            "축구",
            "요약",
            Instant.parse("2025-05-09T00:00:00Z"),
            3L,   // visibleComments
            7L,   // viewCount
            true  // viewedByMe
        )),
        "1",
        Instant.parse("2025-05-09T00:00:00Z"),
        10,
        false,
        1L
    );
    given(pageResponseMapper.fromSlice(
        any(org.springframework.data.domain.Slice.class),
        any(), any(), anyLong()))
        .willReturn(stubResponse);

    // when
    PageResponse<ArticleDto> result = articleService.searchArticles(request);

    // then
    assertThat(result.contents()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1L);
    assertThat(result.contents().get(0).commentCount()).isEqualTo(3L);
    assertThat(result.contents().get(0).viewCount()).isEqualTo(7L);
    assertThat(result.contents().get(0).viewedByMe()).isTrue();

    verify(newsArticleRepository).searchArticles(any(), any(), any(), any(), anyInt());
    verify(newsArticleRepository).countArticles(any());
    verify(commentRepository).countByArticleId(1L);
    verify(commentRepository).countByArticleIdAndIsDeletedFalse(1L);
    verify(articleViewRepository).countByArticleId(1L);
    verify(articleViewRepository).existsByArticleIdAndUserId(1L, 1L);
    verify(pageResponseMapper).fromSlice(any(), any(), any(), anyLong());
  }

  @Test
  @DisplayName("커서 파라미터가 null 일 때 예외 없이 동작")
  void cursor_null_thenNormal() {
    // given: orderBy를 publishDate로 변경
    CursorPageRequestArticleDto request = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "publishDate", "ASC", null, null, 1
    );

    given(newsArticleRepository.searchArticles(any(), any(), any(), any(), anyInt()))
        .willReturn(List.of());
    given(newsArticleRepository.countArticles(any())).willReturn(0L);
    given(pageResponseMapper.fromSlice(any(), any(), any(), anyLong()))
        .willReturn(new PageResponse<>(List.of(), null, null, 1, false, 0L));

    // when
    PageResponse<ArticleDto> result = articleService.searchArticles(request);

    // then
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
  @Test
  @DisplayName("backup 정상 업로드")
  void backup_thenNormal() throws Exception {
    //given
    LocalDate date = LocalDate.of(2025, 5, 2);
    Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<NewsArticle> dummyList = List.of(
        NewsArticle.builder().source("NAVER").sourceUrl("u").articleTitle("t").articlePublishedDate(Instant.now()).articleSummary("s").build()
    );
    when(newsArticleRepository.findAllByCreatedAtBetween(start, end))
        .thenReturn(dummyList);
    when(objectMapper.writeValueAsString(dummyList))
        .thenReturn("[{}]");

    // when
    articleService.backup(date);

    // then
    ArgumentCaptor<PutObjectRequest> reqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(reqCap.capture(), any(RequestBody.class));
    assertThat(reqCap.getValue().bucket()).isEqualTo("test-bucket");
    assertThat(reqCap.getValue().key()).isEqualTo("backup/2025-05-02.json");
  }

  @Test
  @DisplayName("backup JSON 직렬화 실패하면 예외 발생")
  void backup_jsonWriteFail_thenException() throws Exception {
    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("fail"){});
    assertThatThrownBy(() -> articleService.backup(LocalDate.now()))
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("직렬화 오류");
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
  @DisplayName("ID가 있으면 isDeleted=true")
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

  //물리 삭제
  @Test
  @DisplayName("hard : ID가 없으면 예외")
  void hard_noId_thenException(){
    //given

    //when
    when(newsArticleRepository.existsById(1L)).thenReturn(false);

    //then
    assertThrows(NewsException.class,()->articleService.hardDeleteArticle(1L));
  }
  @Test
  @DisplayName("hard : ID가 있으면 삭제")
  void hard_id_thenDelete(){
    //given
    NewsArticle a1 = NewsArticle.from(
        new ExternalNewsItem("Naver","url1","url1","title1",Instant.now(),"desc1"));
    ReflectionTestUtils.setField(a1, "id", 1L);
    when(newsArticleRepository.existsById(1L)).thenReturn(true);

    //when
    articleService.hardDeleteArticle(1L);

    //then
    verify(newsArticleRepository).deleteById(1L);
  }

  //searchArticles
  @Test
  @DisplayName("limit 0 이하면 BadRequest")
  void searchArticles_limit0_thenBadRequest(){
    //given
    CursorPageRequestArticleDto req = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "id", "ASC", null, null, 0
    );
    //when,then
    assertThatThrownBy(() -> articleService.searchArticles(req))
        .isInstanceOf(NewsException.class)
        .matches(e -> ((NewsException)e).getCode() == ErrorCode.NEWS_LIMIT_MORE_THAN_ONE_EXCEPTION);
  }

  @Test
  @DisplayName("searchArticles: 허용되지 않은 orderBy 이면 BAD_REQUEST")
  void searchArticles_invalidOrderBy_thenBadRequest(){
    //given
    CursorPageRequestArticleDto req = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "foo", "DESC", null, null, 5
    );
    //when,then
    assertThatThrownBy(() -> articleService.searchArticles(req))
        .isInstanceOf(NewsException.class)
        .matches(e -> ((NewsException)e).getCode() == ErrorCode.NEWS_ORDERBY_IS_NOT_SUPPORT_EXCEPTION);
  }

  @Test
  @DisplayName("searchArticles: repository 예외 시 INTERNAL_SERVER_ERROR")
  void searchArticles_repoException_thenInternalError() {
    //given
    CursorPageRequestArticleDto req = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "publishDate", "ASC", null, null, 3
    );
    when(newsArticleRepository.searchArticles(any(), any(), any(), any(), anyInt()))
        .thenThrow(new RuntimeException("fail"));

    //when,then
    assertThatThrownBy(() -> articleService.searchArticles(req))
        .isInstanceOf(NewsException.class)
        .matches(e -> ((NewsException)e).getCode() == ErrorCode.NEWS_CALL_NEWSARTICLEREPOSITORY_EXCEPTION);
  }

  @Test
  @DisplayName("searchArticles: cursor 숫자 문자열 파싱 정상")
  void searchArticles_cursorParsing() {
    // given: orderBy를 publishDate로 변경
    CursorPageRequestArticleDto req = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "publishDate", "DESC", "42", Instant.EPOCH, 2
    );

    when(newsArticleRepository.searchArticles(any(), any(), eq(42L), any(), anyInt()))
        .thenReturn(List.of());
    when(newsArticleRepository.countArticles(any())).thenReturn(0L);
    when(pageResponseMapper.fromSlice(any(), any(), any(), anyLong()))
        .thenReturn(new PageResponse<>(List.of(), null, null, 2, false, 0L));

    // when
    articleService.searchArticles(req);

    // then: cursor가 Long 42로 파싱되어 전달됐는지
    verify(newsArticleRepository)
        .searchArticles(any(), any(), eq(42L), any(), anyInt());
  }

  @Test
  @DisplayName("searchArticles: buildOrder 분기(publishDate/viewCount/commentCount) 정상")
  void searchArticles_buildOrderBranches() {
    // 공통 stub
    when(newsArticleRepository.searchArticles(any(), any(), any(), any(), anyInt()))
        .thenReturn(List.of());
    when(newsArticleRepository.countArticles(any())).thenReturn(0L);
    when(pageResponseMapper.fromSlice(any(), any(), any(), anyLong()))
        .thenReturn(new PageResponse<>(List.of(), null, null, 1, false, 0L));

    // publishDate
    CursorPageRequestArticleDto req1 = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "publishDate", "ASC", null, null, 1
    );
    articleService.searchArticles(req1);

    // viewCount
    CursorPageRequestArticleDto req2 = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "viewCount", "DESC", null, null, 1
    );
    articleService.searchArticles(req2);

    // commentCount
    CursorPageRequestArticleDto req3 = new CursorPageRequestArticleDto(
        1L, null, null, null, null, null,
        "commentCount", "DESC", null, null, 1
    );
    articleService.searchArticles(req3);

    // then: 세 번 모두 repository 호출됨
    verify(newsArticleRepository, times(3))
        .searchArticles(any(), any(), any(), any(), anyInt());
  }
}

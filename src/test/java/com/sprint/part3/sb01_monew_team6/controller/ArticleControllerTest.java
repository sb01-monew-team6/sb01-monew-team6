package com.sprint.part3.sb01_monew_team6.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleRestoreResultDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.ArticleService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)   // 테스트에서 보안 필터 제거
@Import(GlobalExceptionHandler.class)
public class ArticleControllerTest {
  @Autowired
  MockMvc mvc;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  ArticleService articleService;

  private final String BASE = "/api/articles";
  private final String HEADER = "Monew-Request-User-ID";

  @Test
  @DisplayName("Get :/api/articles -> 정상 요청 : 200, content 반환")
  void getArticles_success() throws Exception {
    //given
    ArticleDto dto = new ArticleDto(
        10L, "NAVER", "https://n", "테스트제목",
        "요약",Instant.parse("2025-04-30T00:00:00Z"),
        5L, 3L, false
    );
    PageResponse<ArticleDto> response = new PageResponse<>(
        List.of(dto),"10",Instant.parse("2025-04-30T00:00:00Z"),1,false,1L
    );
    given(articleService.searchArticles(any())).willReturn(response);

    //when,then
    mvc.perform(get(BASE)
            .header(HEADER, 1L)
            .param("orderBy", "publishDate")
            .param("direction", "DESC")
            .param("limit", "1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(10))
        .andExpect(jsonPath("$.nextCursor").value("10"))
        .andExpect(jsonPath("$.hasNext").value(false));
  }
  @Test
  @DisplayName("Get :/api/articles -> 필수 헤더 없으면 500")
  void missingHeader() throws Exception {
    mvc.perform(get(BASE)
            .param("orderBy", "publishDate")
            .param("direction", "DESC")
            .param("limit", "1"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Get :/api/articles -> 필수 파라미터(orderBy) 누락 → 500")
  void missingOrderBy() throws Exception {
    mvc.perform(get(BASE)
            .header(HEADER, 1L)
            .param("direction", "ASC")
            .param("limit", "1"))
        .andExpect(status().is5xxServerError());
  }
  @Test
  @DisplayName("Get :/api/articles -> 빈 결과 조회 → 200, contents 빈 배열")
  void empty_returnEmpty() throws Exception {
    PageResponse<ArticleDto> resp = new PageResponse<>(
        List.of(), null, null,
        10, false, 0L
    );
    given(articleService.searchArticles(any()))
        .willReturn(resp);

    mvc.perform(get(BASE)
            .header(HEADER, 1L)
            .param("orderBy", "id")
            .param("direction", "ASC")
            .param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)))
        .andExpect(jsonPath("$.totalElements").value(0));
  }
  //백업 복구
  @Test
  @DisplayName("GET /api/articles/restore 호출 시 정상 JSON 반환")
  void getRestore() throws Exception {
    // given
    LocalDate d = LocalDate.of(2025,4,30);
    var dto = new ArticleRestoreResultDto(d, List.of(1L), 1);
    given(articleService.restore(d, d)).willReturn(List.of(dto));

    // when & then
    mvc.perform(get("/api/articles/restore")
            .param("from","2025-04-30T00:00:00")
            .param("to","2025-04-30T23:59:59"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].restoredArticleCount").value(1));
  }

  //논리 삭제
  @Test
  @DisplayName("DELETE /api/articles/{id} -> 204(No Content)")
  void deleteArticle_api_returnNoContent() throws Exception {
    //given

    //when,then
    mvc.perform(delete("/api/articles/{id}",1L))
        .andExpect(status().isNoContent());

    verify(articleService).deleteArticle(1L);
  }

  @Test
  @DisplayName("DELETE /api/articles/{id} -> ID 없음 -> 404")
  void deleteArticle_api_notFound() throws Exception {
    //given
    doThrow(new NewsException(ErrorCode.NEWS_NOT_USER_FOUND_EXCEPTION,Instant.now(), HttpStatus.NOT_FOUND))
        .when(articleService).deleteArticle(1L);

    //when,then
    mvc.perform(delete("/api/articles/{id}",1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("유저가 존재하지 않습니다."));
  }

  //물리 삭제
  @Test
  @DisplayName("DELETE /api/articles/{id}/hard-> 204(No Content)")
  void deleteArticle_hard_returnNoContent() throws Exception {
    //given


    // when,then
    mvc.perform(delete("/api/articles/{id}/hard",1L))
        .andExpect(status().isNoContent());

    verify(articleService).hardDeleteArticle(1L);
  }
  @Test
  @DisplayName("DELETE /api/articles/{id}/hard -> ID 없음 -> 404")
  void deleteArticle_hard_notFound() throws Exception {
    //given
    doThrow(new NewsException(ErrorCode.NEWS_NOT_USER_FOUND_EXCEPTION,Instant.now(), HttpStatus.NOT_FOUND))
        .when(articleService).hardDeleteArticle(1L);

    //when,then
    mvc.perform(delete("/api/articles/{id}/hard",1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("유저가 존재하지 않습니다."));
  }

  @Test
  @DisplayName("GET /api/articles/restore?date=… → 백업 실행 (200, 텍스트 응답)")
  void restore_backup_onlyDate() throws Exception {
    LocalDate d = LocalDate.of(2025, 4, 30);

    mvc.perform(get(BASE + "/restore")
            .param("date", "2025-04-30"))
        .andExpect(status().isOk())
        .andExpect(content().string("백업 완료: 2025-04-30"));

    verify(articleService).backup(d);
  }

  @Test
  @DisplayName("GET /api/articles/restore 파라미터 오류 → 400")
  void restore_badRequest() throws Exception {
    // date 없이, from/to 둘 다 없이 호출하면 400
    mvc.perform(get(BASE + "/restore"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("파라미터 오류: `date` 또는 `from` & `to` 를 정확히 보내주세요."));
  }

  @Test
  @DisplayName("GET /api/articles 모든 쿼리 파라미터 매핑 확인")
  void getArticles_allParamsMapping() throws Exception {
    // given: 서비스는 빈 페이지 응답
    given(articleService.searchArticles(any()))
        .willReturn(new PageResponse<>(List.of(), null, null, 0, false, 0L));

    // when
    mvc.perform(get(BASE)
            .header(HEADER, 123L)
            .param("keyword", "test")
            .param("interestId", "42")
            .param("sourceIn", "NAVER", "OTHER")
            .param("publishDateFrom", "2025-05-01T12:00:00")
            .param("publishDateTo",   "2025-05-02T15:30:00")
            .param("orderBy", "id")
            .param("direction", "ASC")
            .param("cursor", "77")
            .param("after", "2025-05-01T00:00:00")
            .param("limit", "5"))
        .andExpect(status().isOk());

    // then: 서비스 호출된 DTO 값 검증
    ArgumentCaptor<CursorPageRequestArticleDto> cap =
        ArgumentCaptor.forClass(CursorPageRequestArticleDto.class);
    verify(articleService).searchArticles(cap.capture());

    CursorPageRequestArticleDto dto = cap.getValue();
    assertThat(dto.userId()).isEqualTo(123L);
    assertThat(dto.keyword()).isEqualTo("test");
    assertThat(dto.interestId()).isEqualTo(42L);
    assertThat(dto.sourceIn()).containsExactly("NAVER", "OTHER");
    assertThat(dto.orderBy()).isEqualTo("id");
    assertThat(dto.direction()).isEqualTo("ASC");
    assertThat(dto.cursor()).isEqualTo("77");
    assertThat(dto.limit()).isEqualTo(5);

    // 시간 변환(Asia/Seoul 기준) 체크
    Instant from = dto.publishDateFrom();
    Instant to   = dto.publishDateTo();
    Instant after = dto.after();
    ZoneId seoul = ZoneId.of("Asia/Seoul");
    assertThat(LocalDateTime.ofInstant(from, seoul))
        .isEqualTo(LocalDateTime.parse("2025-05-01T12:00:00"));
    assertThat(LocalDateTime.ofInstant(to, seoul))
        .isEqualTo(LocalDateTime.parse("2025-05-02T15:30:00"));
    assertThat(LocalDateTime.ofInstant(after, seoul))
        .isEqualTo(LocalDateTime.parse("2025-05-01T00:00:00"));
  }
}

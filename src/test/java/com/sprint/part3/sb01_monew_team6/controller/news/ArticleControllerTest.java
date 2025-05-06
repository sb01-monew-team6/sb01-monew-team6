package com.sprint.part3.sb01_monew_team6.controller.news;

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
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.service.news.ArticleService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        .andExpect(status().is5xxServerError());
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
}

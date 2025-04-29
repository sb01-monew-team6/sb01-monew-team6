//package com.sprint.part3.sb01_monew_team6.controller;
//
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
//import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
//import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
//import com.sprint.part3.sb01_monew_team6.service.ArticleViewService;
//import java.time.Instant;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(ArticleViewController.class)
//@ActiveProfiles("test")
//@AutoConfigureMockMvc(addFilters = false)
//public class ArticleViewControllerTest {
//  @Autowired
//  MockMvc mvc;
//
//  @Autowired
//  private ObjectMapper objectMapper;  // JSON 직렬화/역직렬화
//
//  @MockitoBean
//  ArticleViewService articleViewService;
//  @Autowired
//  private MockMvc mockMvc;
//
//  @Test
//  @DisplayName("유효한 articleId,userId로 조회 요청 시 200과 DTO 반환")
//  void viewArticle_success() throws Exception {
//    //given
//    Instant now = Instant.parse("2025-04-27T12:00:00Z");
//    ArticleViewDto dto = ArticleViewDto.builder()
//        .id(10L)
//        .viewedBy(2L)
//        .createdAt(now)
//        .articleId(1L)
//        .source("Naver")
//        .sourceUrl("https://test.api.com")
//        .articleTitle("테스트기사")
//        .articlePublishedDate(now.toString())
//        .articleSummary("요약")
//        .articleCommentCount(0L)
//        .articleViewCount(1L)
//        .build();
//    given(articleViewService.viewArticle(1L, 2L)).willReturn(dto);
//
//    //when
//    mockMvc.perform(post("/api/articles/{articleId}/article-views",1L)
//        .param("userId","2")
//        .accept(MediaType.APPLICATION_JSON)
//    )
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$.articleId").value(1))
//        .andExpect(jsonPath("$.viewedBy").value(2))
//        .andExpect(jsonPath("$.articleViewCount").value(1));
//  }
//
//  @Test
//  @DisplayName("중복 조회 시에도 200 과 기존 DTO 반환")
//  void viewArticle_duplicate() throws Exception{
//    //given
//    Instant now = Instant.parse("2025-04-27T12:00:00Z");
//    ArticleViewDto dto = ArticleViewDto.builder()
//        .id(10L)
//        .viewedBy(2L)
//        .createdAt(now)
//        .articleId(1L)
//        .source("Naver")
//        .sourceUrl("https://test.api.com")
//        .articleTitle("테스트기사")
//        .articlePublishedDate(now.toString())
//        .articleSummary("요약")
//        .articleCommentCount(0L)
//        .articleViewCount(3L)   // 이미 3번 조회
//        .build();
//    given(articleViewService.viewArticle(1L, 2L)).willReturn(dto);
//
//    //when : 중복 조회 요청
//    mockMvc.perform(post("/api/articles/{articleId}/article-views", 1L)
//        .param("userId", "2")
//        .accept(MediaType.APPLICATION_JSON)
//    )
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.articleViewCount").value(3));
//  }
//
//  @Test
//  @DisplayName("존재하지 않는 userId면 404 에러")
//  void viewArticle_userNotFound() throws Exception {
//    // given: 서비스가 NotFoundException
//    given(articleViewService.viewArticle(anyLong(), anyLong()))
//        .willThrow(new NewsException(ErrorCode.NEWS_NOT_USER_FOUND_EXCEPTION,Instant.now(),
//            HttpStatus.NOT_FOUND));
//
//    // when & then: 404와 에러 메시지 반환
//    mockMvc.perform(post("/api/articles/{articleId}/article-views", 1L)
//            .param("userId", "2")
//            .accept(MediaType.APPLICATION_JSON)
//        )
//        .andExpect(status().isNotFound())
//        .andExpect(jsonPath("$.message").value("유저가 존재하지 않습니다."));
//  }
//
//}

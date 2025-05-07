package com.sprint.part3.sb01_monew_team6.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CollectResponse;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import com.sprint.part3.sb01_monew_team6.service.impl.NewsCollectionServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NewsCollectionController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
public class NewsCollectionControllerTest {
  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper objectMapper;
  @MockitoBean
  private UserRepository userRepository;
  @MockitoBean
  NewsCollectionServiceImpl service;

  @Test
  @DisplayName("정상 서비스 → When GET /collect-news → Then 200 OK")
  void getNewsCollectionSuccess() throws Exception {
    //given
    willReturn(Optional.empty()).given(service).collectAndSave();

    //when

    //then
    mvc.perform(get("/api/articles/collect/news").with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /api/articles/collect/news → 202 + 메시지 반환")
  void collectNews_success() throws Exception {
    // given
    Instant published = Instant.parse("2025-04-27T12:00:00Z");

    Long articleId = 1L;
    NewsArticle article = NewsArticle.builder()
        .source("Naver")
        .sourceUrl("https://test.api.com")
        .articleTitle("test")
        .articlePublishedDate(published)
        .articleSummary("test")
        .isDeleted(false)
        .build();
    ReflectionTestUtils.setField(article, "id", articleId);

    List<NewsArticle> savedList = List.of(article);
    given(service.collectAndSave()).willReturn(Optional.of(savedList));

    ArticleDto dto = ArticleDto.builder()
        .id(1L)
        .source("Naver")
        .sourceUrl("https://test.api.com")
        .title("test")
        .summary("test")
        .publishDate(published)
        .commentCount(0L)
        .viewCount(0L)
        .viewedByMe(false)
        .build();

    CollectResponse expected = CollectResponse.builder()
        .message("배치 작업이 실행되었습니다.")
        .count(1)
        .articles(List.of(dto))
        .build();

    String expectedJson = objectMapper.writeValueAsString(expected);

    // when / then
    mvc.perform(post("/api/articles/collect/news").with(csrf()))
        .andExpect(status().isAccepted())                            // 202 Accepted
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedJson));
  }
}

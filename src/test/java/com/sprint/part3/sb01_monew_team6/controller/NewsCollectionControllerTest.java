package com.sprint.part3.sb01_monew_team6.controller;

import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NewsCollectionController.class)
@WithMockUser
public class NewsCollectionControllerTest {
  @Autowired
  MockMvc mvc;
  @MockitoBean
  NewsCollectionService service;

  @Test
  @DisplayName("정상 서비스 → When GET /collect-news → Then 200 OK")
  void getNewsCollectionSuccess() throws Exception {
    //given
    willDoNothing().given(service).collectAndSave();

    //when

    //then
    mvc.perform(get("/api/articles/collect/news").with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /api/articles/collect/news → 202 + 메시지 반환")
  void collectNews_success() throws Exception {
    // given
    willDoNothing().given(service).collectAndSave();

    // when / then
    mvc.perform(post("/api/articles/collect/news").with(csrf()))
        .andExpect(status().isAccepted())                            // 202 Accepted
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Batch job triggered"));
  }
}

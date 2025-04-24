package com.sprint.part3.sb01_monew_team6.controller;

import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NewsCollectionController.class)
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
    mvc.perform(get("/api/admin/collect-news"))
        .andExpect(status().isOk());
  }
}

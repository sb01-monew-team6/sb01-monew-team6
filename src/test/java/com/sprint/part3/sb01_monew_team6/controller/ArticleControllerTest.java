package com.sprint.part3.sb01_monew_team6.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.part3.sb01_monew_team6.controller.news.ArticleController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleController.class)
public class ArticleControllerTest {
  @Autowired
  MockMvc mvc;

  @Test
  @DisplayName(" Get : /api/articles -> 200 OK")
  void missingParams() throws Exception {
    mvc.perform(get("/api/articles"))
        .andExpect(status().isOk());
  }
}

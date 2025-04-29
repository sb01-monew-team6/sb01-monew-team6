package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("댓글 등록 실패 - content가 비어있을 경우 400 반환")
    @WithMockUser
    void registerComment_withBlankContent_shouldReturnBadRequest() throws Exception {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(
                1L, 1L, " " // 빈 문자열
        );
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 등록 성공 - 유효한 요청 시 201 Created 반환")
    @Test
    @WithMockUser
    void registerComment_withValidRequest_shouldReturnCreated() throws Exception {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(
                1L, 1L, "정상 댓글입니다."
        );
        String requestBody = objectMapper.writeValueAsString(request);

        CommentDto responseDto = new CommentDto(
                1L, 1L, 1L, "작성자닉네임", "정상 댓글입니다.", 0L, false, Instant.now()
        );

        when(commentService.register(any(CommentRegisterRequest.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.id()))
                .andExpect(jsonPath("$.content").value(responseDto.content()));
    }

    @DisplayName("댓글 목록 조회 실패 - 필수 파라미터 누락 시 400 반환")
    @Test
    @WithMockUser
    void getComments_withMissingRequiredParams_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/comments")
                        .with(csrf())
                        .header("Monew-Request-User-ID", 1L)) // orderBy, direction, limit 없음
                .andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 목록 조회 성공 - 필수 파라미터를 모두 제공할 경우 200 OK 반환")
    @Test
    @WithMockUser
    void getComments_withValidParams_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/comments")
                        .param("orderBy", "createdAt")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .header("Monew-Request-User-ID", "1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }



}

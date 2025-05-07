package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.MonewRequestUserInterceptor;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.*;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentException;
import com.sprint.part3.sb01_monew_team6.service.CommentLikeService;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;



@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {


    @MockitoBean
    private MonewRequestUserInterceptor monewRequestUserInterceptor;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockBean
    private CommentLikeService commentLikeService;

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

    @DisplayName("댓글 목록 조회 실패 - 잘못된 orderBy 값일 경우 400 반환")
    @Test
    @WithMockUser
    void getComments_withInvalidOrderBy_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/comments")
                        .param("orderBy", "invalidValue") // 잘못된 orderBy
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .header("Monew-Request-User-ID", "1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 목록 조회 실패 - 잘못된 direction 값일 경우 400 반환")
    @Test
    @WithMockUser
    void getComments_withInvalidDirection_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/comments")
                        .param("orderBy", "createdAt")
                        .param("direction", "UPWARD") // 잘못된 direction
                        .param("limit", "10")
                        .header("Monew-Request-User-ID", "1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 목록 조회 실패 - 잘못된 articleId일 경우 400 반환")
    @Test
    @WithMockUser
    void getComments_withInvalidArticleId_shouldReturnBadRequest() throws Exception {
        // given
        when(commentService.findAll(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new CommentException(ErrorCode.COMMENT_NOT_FOUND, Instant.now(), HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/api/comments")
                .param("articleId", "99999")
                .param("orderBy", "createdAt")
                .param("direction", "DESC")
                .param("limit", "10")
                .header("Monew-Request-User-ID", "1")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 목록 조회 실패 - 잘못된 limit 값일 경우 400 반환")
    @Test
    @WithMockUser
    void getComments_withInvalidLimit_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/comments")
                .param("orderBy", "createdAt")
                .param("direction", "DESC")
                .param("limit", "-10") // 잘못된 limit 값
                .header("Monew-Request-User-ID", "1")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 목록 조회 실패 - 헤더 값 누락 시 400 반환")
    @Test
    @WithMockUser
    void getComment_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/comments")
                .param("orderBy", "createdAt")
                .param("direction", "DESC")
                .param("limit", "10")
                .with(csrf())) // 헤더 값
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

    @DisplayName("댓글 목록 조회 성공 - 데이터가 존재할 경우 200 OK와 목록 반환")
    @Test
    @WithMockUser
    void getComments_withValidParams_shouldReturnCommentList() throws Exception {
        // given
        CommentDto comment1 = new CommentDto(1L, 1L, 1L, "작성자1", "댓글 내용1", 5L, false, Instant.now());
        CommentDto comment2 = new CommentDto(2L, 1L, 2L, "작성자2", "댓글 내용2", 3L, false, Instant.now());
        List<CommentDto> commentList = List.of(comment1, comment2);
        PageResponse<CommentDto> mockPage = new PageResponse<>(
                commentList,         // contents
                "2024-05-01T12:00:00Z", // nextCursor
                true,                // nextAfter
                commentList.size(),  // size
                true,                // hasNext
                null                 // totalElements
        );

        when(commentService.findAll(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("orderBy", "createdAt")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .header("Monew-Request-User-ID", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(commentList.size()))
                .andExpect(jsonPath("$.content[0].content").value(comment1.content()))
                .andExpect(jsonPath("$.content[1].content").value(comment2.content()))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").exists());
    }

    @DisplayName("댓글 목록 조회 - 최대 limit 값일 경우")
    @Test
    @WithMockUser
    void getComments_withMaxLimit_shouldReturnPagedComments() throws Exception {
        mockMvc.perform(get("/api/comments")
                .param("orderBy", "createdAt")
                .param("direction", "DESC")
                .param("limit", "1000") // 최대값
                .header("Monew-Request-User-ID", "1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 좋아요 등록 - 성공 응답 반환")
    void likeComment_shouldReturn200() throws Exception {
        // given
        CommentLikeDto response = CommentLikeDto.builder()
                .id(1L)
                .likedBy(100L)
                .createdAt(Instant.now())
                .commentId(1L)
                .articleId(2L)
                .commentUserId(100L)
                .commentUserNickname("사용자")
                .commentContent("좋은 기사네요")
                .commentLikeCount(1L)
                .commentCreatedAt(Instant.now())
                .build();

        Mockito.when(commentService.likeComment(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/comments/1/comment-likes")
                        .header("Monew-Request-User-ID", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(1L))
                .andExpect(jsonPath("$.likedBy").value(100L));
    }
//----------------------------------------------------------------------------------------------------------------------
    @DisplayName("DELETE /api/comments/{id} - 요청시 정상응답 204 반환")
    @Test
    void deleteComment() throws Exception {
        //given
        Long commentId = 1L;

        //when
        doNothing().when(commentService).softDeleteComment(commentId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/" + commentId))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @DisplayName("댓글 수정 성공 - PATCH /api/comments/{commentId}")
    @Test
    @WithMockUser
    void updateComment_withValidRequest_shouldReturnOk() throws Exception {
        // given
        Long commentId = 1L;
        Long userId = 10L;
        String updatedContent = "수정된 댓글 내용";

        CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);
        String requestBody = objectMapper.writeValueAsString(request);

        CommentDto responseDto = CommentDto.builder()
            .id(commentId)
            .articleId(100L)
            .userId(userId)
            .userNickname("작성자")
            .content(updatedContent)
            .likeCount(5L)
            .likedByMe(false)
            .createdAt(Instant.now())
            .build();

        when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/comments/{commentId}", commentId)
                .header("Monew-Request-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value(updatedContent));
    }

    @DisplayName("댓글 물리 삭제 성공 - DELETE /api/comments/{commentId}/hard 요청시 204 반환")
    @Test
    @WithMockUser
    void hardDeleteComment_shouldReturnNoContent() throws Exception {
        // given
        Long commentId = 1L;

        // mock 설정
        doNothing().when(commentService).hardDelete(commentId);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/comments/{commentId}/hard", commentId)
                .with(csrf()))
            .andExpect(status().isNoContent());
    }



    @Test
    @DisplayName("댓글 좋아요를 취소할 수 있다")
    void cancelCommentLike() throws Exception {
        // given
        Long commentId = 1L;
        Long userId = 2L;

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
                        .header("Monew-Request-User-ID", userId))
                .andExpect(status().isOk());
    }
}

package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.MonewRequestUserInterceptor;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.CursorPageResponseInterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestCreateRequestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestUpdateRequestDto;
import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.service.InterestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InterestController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class InterestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  // MonewRequestUserInterceptor 에서 UserRepository를 주입받기 때문에
  // 테스트 시에는 더미(Mock) 빈으로 등록해줍니다.
  @MockBean
  private MonewRequestUserInterceptor monewRequestUserInterceptor;

  @MockBean
  private InterestService interestService;

  @Test
  @DisplayName("유효한 정보로 관심사 등록 요청 시 201 Created와 InterestDto 반환")
  @WithMockUser
  void createInterest_withValidRequest_shouldReturnCreatedAndInterestDto() throws Exception {
    // given
    List<String> keywords = List.of("여행", "숙소");
    InterestCreateRequestDto requestDto = new InterestCreateRequestDto("국내여행", keywords);
    String requestJson = objectMapper.writeValueAsString(requestDto);

    Interest createdInterest = Interest.builder()
        .name(requestDto.name())
        .keywords(String.join(",", keywords))
        .subscriberCount(0L)
        .build();
    ReflectionTestUtils.setField(createdInterest, "id", 1L);
    ReflectionTestUtils.setField(createdInterest, "createdAt", Instant.now());

    InterestDto expectedResponseDto = InterestDto.fromEntity(createdInterest, false);

    when(interestService.createInterest(eq(requestDto.name()), eq(requestDto.keywords())))
        .thenReturn(createdInterest);

    // when
    ResultActions actions = mockMvc.perform(post("/api/interests")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(requestJson)
        .with(csrf()));

    // then
    actions.andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(expectedResponseDto.id().intValue())))
        .andExpect(jsonPath("$.name", is(expectedResponseDto.name())))
        .andExpect(jsonPath("$.keywords[0]", is(expectedResponseDto.keywords().get(0))))
        .andExpect(jsonPath("$.subscriberCount", is(expectedResponseDto.subscriberCount().intValue())))
        .andExpect(jsonPath("$.createdAt").exists());

    verify(interestService).createInterest(eq(requestDto.name()), eq(requestDto.keywords()));
  }

  @Test
  @DisplayName("성공: 관심사 목록 조회 요청 시 200 OK와 CursorPageResponseInterestDto 반환")
  @WithMockUser
  void getInterests_shouldReturnOkAndCursorPageResponse() throws Exception {
    // given
    List<InterestDto> interests = List.of(
        new InterestDto(
            1L,
            "코딩",
            List.of("개발", "자바"),
            10L,
            true,
            Instant.parse("2024-05-01T00:00:00Z"),
            Instant.parse("2024-05-02T00:00:00Z")
        ),
        new InterestDto(
            2L,
            "독서",
            List.of("소설", "에세이"),
            10L,
            true,
            Instant.parse("2024-05-01T00:00:00Z"),
            Instant.parse("2024-05-02T00:00:00Z")
        )
    );

    CursorPageResponseInterestDto response = new CursorPageResponseInterestDto(
        interests,
        "2025-05-01T00:00:00Z", // nextCursor
        "2025-05-01T00:00:00Z", // nextAfter
        interests.size(),
        2L, // totalElements
        false // hasNext
    );

    when(interestService.findAll(any(), any(), any(), any(), any(), any(), anyInt()))
        .thenReturn(response);

    // when & then
    mockMvc.perform(get("/api/interests")
            .header("Monew-Request-User-ID", "1")
            .param("size", "2")
            .param("orderBy", "name")
            .param("direction", "ASC"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("코딩"))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  @DisplayName("성공(GREEN): 존재하는 관심사 ID로 삭제 요청 시 204 No Content 반환")
  @WithMockUser
  void deleteInterest_whenInterestExists_shouldReturnNoContent() throws Exception {
    // given
    Long interestId = 1L;
    doNothing().when(interestService).deleteInterest(eq(interestId));

    // when
    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}", interestId)
        .with(csrf()));

    // then
    actions.andExpect(status().isNoContent());
    verify(interestService).deleteInterest(eq(interestId));
  }

  @Test
  @DisplayName("실패(RED): 존재하지 않는 관심사 ID로 삭제 요청 시 404 Not Found 반환")
  @WithMockUser
  void deleteInterest_whenInterestNotFound_shouldReturnNotFound() throws Exception {
    // given
    Long nonExistentInterestId = 999L;
    doThrow(new InterestNotFoundException(nonExistentInterestId))
        .when(interestService).deleteInterest(eq(nonExistentInterestId));

    // when
    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}", nonExistentInterestId)
        .with(csrf()));

    // then
    actions.andExpect(status().isNotFound());
    verify(interestService).deleteInterest(eq(nonExistentInterestId));
  }

  @Test
  @DisplayName(" 유효한 정보로 관심사 수정 요청 시 200 OK와 InterestDto 반환")
  @WithMockUser
  void updateInterest_withValidRequest_shouldReturnOkAndInterestDto() throws Exception {
    // given
    Long id = 1L;
    InterestUpdateRequestDto requestDto = new InterestUpdateRequestDto(
        "업데이트된 관심사", List.of("새키워드1","새키워드2")
    );
    String requestJson = objectMapper.writeValueAsString(requestDto);

    Interest updated = Interest.builder()
        .name(requestDto.name())
        .keywords(String.join(",", requestDto.keywords()))
        .subscriberCount(5L)
        .build();
    ReflectionTestUtils.setField(updated, "id", id);
    ReflectionTestUtils.setField(updated, "createdAt", Instant.now().minusSeconds(200));
    ReflectionTestUtils.setField(updated, "updatedAt", Instant.now());

    when(interestService.updateInterest(eq(id), any(InterestUpdateRequestDto.class)))
        .thenReturn(updated);

    // when & then
    mockMvc.perform(patch("/api/interests/{interestId}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(id.intValue())))
        .andExpect(jsonPath("$.name", is(requestDto.name())))
        .andExpect(jsonPath("$.keywords[0]", is(requestDto.keywords().get(0))))
        .andExpect(jsonPath("$.subscriberCount", is(5)))
        .andExpect(jsonPath("$.updatedAt").exists());

    verify(interestService).updateInterest(eq(id), any(InterestUpdateRequestDto.class));
  }
  @Test
  @DisplayName("성공: 관심사 삭제 시 204 No Content 반환")
  @WithMockUser
  void deleteInterest_shouldReturnNoContent() throws Exception {
    Long interestId = 1L;

    // given: 아무것도 반환하지 않으므로 doNothing()
    doNothing().when(interestService).deleteInterest(interestId);

    // when
    ResultActions result = mockMvc.perform(delete("/api/interests/{interestId}", interestId)
        .header("Monew-Request-User-ID", "1"));

    // then
    result.andExpect(status().isNoContent());

    verify(interestService).deleteInterest(interestId);
  }
}

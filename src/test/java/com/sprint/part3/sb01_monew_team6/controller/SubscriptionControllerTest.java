package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto; // SubscriptionDto 임포트
import com.sprint.part3.sb01_monew_team6.entity.Interest; // DTO 생성 시 필요
import com.sprint.part3.sb01_monew_team6.entity.Subscription; // DTO 생성 시 필요
import com.sprint.part3.sb01_monew_team6.entity.User; // DTO 생성 시 필요
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser; // 헤더 사용으로 변경
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant; // DTO 생성 시 필요
import java.util.List; // DTO 생성 시 필요

import static org.hamcrest.Matchers.is; // jsonPath 검증용
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(SubscriptionController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class SubscriptionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SubscriptionService subscriptionService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("성공: 헤더에 유효한 사용자 ID 포함하여 관심사 구독 요청 시 200 OK와 SubscriptionDto 반환")
    // @WithMockUser 제거 -> 헤더 사용
  void subscribeInterest_withValidHeader_shouldReturnOkAndDto() throws Exception {
    // given
    Long interestId = 1L;
    Long userId = 123L; // 헤더로 전달될 사용자 ID

    // 서비스가 반환할 DTO 준비
    SubscriptionDto mockReturnDto = SubscriptionDto.builder()
        .id(100L) // 예시 ID
        .interestId(interestId)
        .interestName("테스트 관심사")
        .interestKeywords(List.of("테스트", "키워드"))
        .interestSubscriberCount(1L)
        .createdAt(Instant.now())
        .build();

    // Mockito 설정: subscriptionService.subscribe()가 호출되면 DTO 반환
    when(subscriptionService.subscribe(eq(userId), eq(interestId))).thenReturn(mockReturnDto);

    // when: POST 요청 수행 (헤더 포함)
    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId) // <<<--- 헤더 추가
        .with(csrf()));

    // then: 상태 코드, 컨텐츠 타입, 응답 본문 검증
    actions.andExpect(status().isOk()) // <<<--- 200 OK 기대
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(mockReturnDto.id().intValue()))) // <<<--- 응답 본문 검증 추가
        .andExpect(jsonPath("$.interestId", is(interestId.intValue())))
        .andExpect(jsonPath("$.interestName", is(mockReturnDto.interestName())))
        .andExpect(jsonPath("$.interestKeywords[0]", is(mockReturnDto.interestKeywords().get(0))))
        .andExpect(jsonPath("$.interestSubscriberCount", is(mockReturnDto.interestSubscriberCount().intValue())))
        .andExpect(jsonPath("$.createdAt").exists());


    // 서비스 메서드가 올바른 인자(userId, interestId)로 호출되었는지 검증
    verify(subscriptionService).subscribe(eq(userId), eq(interestId));
  }

  @Test
  @DisplayName("성공: 헤더에 유효한 사용자 ID 포함하여 관심사 구독 취소 요청 시 204 No Content 반환")
    // @WithMockUser 제거 -> 헤더 사용
  void unsubscribeInterest_withValidHeader_shouldReturnNoContent() throws Exception {
    // given
    Long interestId = 1L;
    Long userId = 123L; // 헤더로 전달될 사용자 ID

    // Mockito 설정: subscriptionService.unsubscribe()가 호출되면 아무 일도 일어나지 않음 (void)
    doNothing().when(subscriptionService).unsubscribe(eq(userId), eq(interestId));

    // when: DELETE 요청 수행 (헤더 포함)
    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId) // <<<--- 헤더 추가
        .with(csrf()));

    // then: 상태 코드 검증 및 서비스 메서드 호출 검증
    actions.andExpect(status().isNoContent()); // 204 No Content 기대

    // 서비스 메서드가 올바른 인자(userId, interestId)로 호출되었는지 검증
    verify(subscriptionService).unsubscribe(eq(userId), eq(interestId));
  }
  @Test
  @DisplayName("실패(RED): 사용자 ID 헤더 누락 시 구독 요청하면 400 Bad Request 반환")
  void subscribeInterest_whenUserIdHeaderIsMissing_shouldReturnBadRequest() throws Exception {
    // given
    Long interestId = 1L;

    // when: POST 요청 수행 (헤더 누락)
    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        // .header("Monew-Request-User-ID", userId) // 헤더 제외
        .with(csrf()));

    // then: 상태 코드 검증 (400 Bad Request 기대)
    actions.andExpect(status().isBadRequest());

    // 서비스 메서드는 호출되지 않아야 함
    verify(subscriptionService, never()).subscribe(anyLong(), anyLong());
  }

  @Test
  @DisplayName("실패(RED): 사용자 ID 헤더 누락 시 구독 취소 요청하면 400 Bad Request 반환")
  void unsubscribeInterest_whenUserIdHeaderIsMissing_shouldReturnBadRequest() throws Exception {
    // given
    Long interestId = 1L;

    // when: DELETE 요청 수행 (헤더 누락)
    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
        // .header("Monew-Request-User-ID", userId) // 헤더 제외
        .with(csrf()));

    // then: 상태 코드 검증 (400 Bad Request 기대)
    actions.andExpect(status().isBadRequest());

    // 서비스 메서드는 호출되지 않아야 함
    verify(subscriptionService, never()).unsubscribe(anyLong(), anyLong());
  }
  @Test
  @DisplayName("실패(RED): 존재하지 않는 관심사 ID로 구독 요청 시 404 Not Found 반환")
  void subscribeInterest_whenInterestNotFound_shouldReturnNotFound() throws Exception {
    // given
    Long nonExistentInterestId = 999L;
    Long userId = 123L;

    // Mockito 설정: subscriptionService.subscribe() 호출 시 InterestNotFoundException 발생
    when(subscriptionService.subscribe(eq(userId), eq(nonExistentInterestId)))
        .thenThrow(new InterestNotFoundException(nonExistentInterestId)); // 예외 발생 Mocking

    // when: POST 요청 수행 (헤더 포함)
    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", nonExistentInterestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf()));

    // then: 상태 코드 검증 (404 Not Found 기대)
    // !!주의!!: GlobalExceptionHandler에 InterestNotFoundException 처리가 없으면 500 Internal Server Error가 발생할 수 있음
    actions.andExpect(status().isNotFound()); // <<<--- 404 Not Found 기대

    // 서비스 메서드가 호출되었는지 검증
    verify(subscriptionService).subscribe(eq(userId), eq(nonExistentInterestId));
  }
  @Test
  @DisplayName("실패(RED): 존재하지 않는 사용자 ID로 구독 요청 시 404 Not Found 반환")
  void subscribeInterest_whenUserNotFound_shouldReturnNotFound() throws Exception {
    // given
    Long interestId = 1L;
    Long nonExistentUserId = 999L; // 존재하지 않는 사용자 ID

    // Mockito 설정: subscriptionService.subscribe() 호출 시 UserNotFoundException 발생
    when(subscriptionService.subscribe(eq(nonExistentUserId), eq(interestId)))
        .thenThrow(new UserNotFoundException(nonExistentUserId)); // 예외 발생 Mocking

    // when: POST 요청 수행 (헤더 포함)
    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", nonExistentUserId) // 존재하지 않는 사용자 ID 헤더
        .with(csrf()));

    // then: 상태 코드 검증 (404 Not Found 기대)
    // !!주의!!: GlobalExceptionHandler에 UserNotFoundException 처리가 없으면 500 Internal Server Error가 발생할 수 있음
    actions.andExpect(status().isNotFound()); // <<<--- 404 Not Found 기대

    // 서비스 메서드가 호출되었는지 검증
    verify(subscriptionService).subscribe(eq(nonExistentUserId), eq(interestId));
  }
  @Test
  @DisplayName("실패(RED): 존재하지 않는 구독 정보로 구독 취소 요청 시 404 Not Found 반환")
  void unsubscribeInterest_whenSubscriptionNotFound_shouldReturnNotFound() throws Exception {
    // given
    Long interestId = 1L;
    Long userId = 123L;

    // Mockito 설정: subscriptionService.unsubscribe() 호출 시 SubscriptionNotFoundException 발생
    doThrow(new SubscriptionNotFoundException(userId, interestId)) // <<<--- 예외 발생 Mocking
        .when(subscriptionService).unsubscribe(eq(userId), eq(interestId));

    // when: DELETE 요청 수행 (헤더 포함)
    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf()));

    // then: 상태 코드 검증 (404 Not Found 기대)
    // !!주의!!: GlobalExceptionHandler에 SubscriptionNotFoundException 처리가 없으면 500 Internal Server Error가 발생할 수 있음
    actions.andExpect(status().isNotFound()); // <<<--- 404 Not Found 기대

    // 서비스 메서드가 호출되었는지 검증
    verify(subscriptionService).unsubscribe(eq(userId), eq(interestId));
  }
}

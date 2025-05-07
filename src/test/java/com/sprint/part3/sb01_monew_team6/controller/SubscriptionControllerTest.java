package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.sprint.part3.sb01_monew_team6.config.MonewRequestUserInterceptor;


@WebMvcTest(SubscriptionController.class)
@MockBean(InterestRepository.class)
@MockBean(MonewRequestUserInterceptor.class)
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
  @DisplayName("유효한 헤더로 관심사 구독 시 200 OK와 SubscriptionDto 반환")
  void subscribe_withValidHeader_returnsOkAndDto() throws Exception {
    Long interestId = 1L;
    Long userId = 42L;

    SubscriptionDto dto = SubscriptionDto.builder()
        .id(100L)
        .interestId(interestId)
        .interestName("테스트")
        .interestKeywords(List.of("키1", "키2"))
        .interestSubscriberCount(5L)
        .createdAt(Instant.now())
        .build();

    when(subscriptionService.subscribe(eq(userId), eq(interestId))).thenReturn(dto);

    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
    );

    actions.andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    verify(subscriptionService).subscribe(eq(userId), eq(interestId));
  }

  @Test
  @DisplayName("성공: 유효한 헤더로 관심사 구독 취소 시 204 No Content 반환")
  void unsubscribe_withValidHeader_returnsNoContent() throws Exception {
    Long interestId = 1L;
    Long userId = 42L;

    doNothing().when(subscriptionService).unsubscribe(eq(userId), eq(interestId));

    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf())
    );

    actions.andExpect(status().isNoContent());
    verify(subscriptionService).unsubscribe(eq(userId), eq(interestId));
  }

  @Test
  @DisplayName("실패: 헤더 누락 시 구독 요청하면 400 Bad Request")
  void subscribe_withoutHeader_returnsBadRequest() throws Exception {
    Long interestId = 1L;

    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        .with(csrf())
    );

    actions.andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("실패: 헤더 누락 시 구독 취소 요청하면 400 Bad Request")
  void unsubscribe_withoutHeader_returnsBadRequest() throws Exception {
    Long interestId = 1L;

    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
        .with(csrf())
    );

    actions.andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("실패: 존재하지 않는 관심사 구독 시 404 Not Found")
  void subscribe_nonExistentInterest_returnsNotFound() throws Exception {
    Long interestId = 999L;
    Long userId = 42L;

    when(subscriptionService.subscribe(eq(userId), eq(interestId)))
        .thenThrow(new InterestNotFoundException(interestId));

    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf())
    );

    actions.andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("존재하지 않는 사용자 구독 시 404 Not Found")
  void subscribe_nonExistentUser_returnsNotFound() throws Exception {
    Long interestId = 1L;
    Long userId = 999L;

    when(subscriptionService.subscribe(eq(userId), eq(interestId)))
        .thenThrow(new UserNotFoundException(userId));

    ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf())
    );

    actions.andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("구독 정보 없을 때 취소 시 404 Not Found")
  void unsubscribe_subscriptionNotFound_returnsNotFound() throws Exception {
    Long interestId = 1L;
    Long userId = 42L;

    doThrow(new SubscriptionNotFoundException(userId, interestId))
        .when(subscriptionService).unsubscribe(eq(userId), eq(interestId));

    ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
        .header("Monew-Request-User-ID", userId)
        .with(csrf())
    );

    actions.andExpect(status().isNotFound());
  }
  @Test
  @DisplayName("DELETE /api/interests/{interestId}/subscriptions 요청 시 204 No Content 반환")
  void unsubscribeInterest_validHeader_shouldReturnNoContent() throws Exception {
    Long interestId = 42L;
    Long userId = 123L;
    doNothing().when(subscriptionService).unsubscribe(eq(userId), eq(interestId));

    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
            .header("Monew-Request-User-ID", userId)
            .with(csrf()))
        .andExpect(status().isNoContent());

    verify(subscriptionService).unsubscribe(eq(userId), eq(interestId));
  }

}

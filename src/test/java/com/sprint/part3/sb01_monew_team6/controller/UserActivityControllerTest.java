package com.sprint.part3.sb01_monew_team6.controller;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;
import com.sprint.part3.sb01_monew_team6.service.UserActivityService;

@WebMvcTest(UserActivityController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class UserActivityControllerTest {

	@MockitoBean
	private UserActivityService userActivityService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("findUserActivityByUserId 호출 시 userId 가 유효하지 않으면 NotificationInvalidException 에러 반환")
	public void respondErrorResponseWhenUserIdIsInvalidWhileFindByUserId() throws Exception {
	    //given & when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/user-activities/{userId}", 0L)
				.header("Monew-Request-User-Id", 0L)
				.with(csrf())
		);

		//then
		ErrorCode notificationInvalidException = USER_ACTIVITY_INVALID_EXCEPTION;

		perform.andExpect(jsonPath("$.status").value(equalTo(BAD_REQUEST.value())))
			.andExpect(jsonPath("$.code").value(equalTo(notificationInvalidException.toString())))
			.andExpect(jsonPath("$.message").value(equalTo(notificationInvalidException.getMessage())));

	}

	@Test
	@DisplayName("findUserActivityByUserId 정상 호출 시 정상적으로 service 호출")
	public void respondPageResponseWhenFindByUserIdSuccessfully() throws Exception {
	    //given
		Long userId = 1L;
		when(userActivityService.findByUserId(eq(userId))).thenReturn(any());

		//when
		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/user-activities/{userId}", userId)
				.header("Monew-Request-User-Id", userId)
				.with(csrf())
		);

		//then
		verify(userActivityService, times(1)).findByUserId(userId);
	}
}
package com.sprint.part3.sb01_monew_team6.controller;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;

@WebMvcTest(controllers = NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

	private final NotificationService notificationService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("findAllByUserId 호출 시 userId 가 0 이하면 NotificationInvalidException 에러 반환")
	public void respondErrorResponseWhenUserIdIsNullWhileFindAllByUserId() throws Exception {

		//given & when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/v1/notifications")
				.header("Monew-Request-User-Id", 0L)
		);

		//then
		ErrorCode notificationInvalidException = NOTIFICATION_INVALID_EXCEPTION;

		perform.andExpect(jsonPath("$.status").value(equalTo(BAD_REQUEST.value())))
			.andExpect(jsonPath("$.code").value(equalTo(notificationInvalidException.toString())))
			.andExpect(jsonPath("$.message").value(equalTo(notificationInvalidException.getMessage())));
	}

	@Test
	@DisplayName("findAllByUserId 정상 호출 시 정상 페이지네이션 반환")
	public void respondPageResponseWhenFindAllByUserIdSuccessfully() throws Exception {

		//given
		NotificationDto notificationDto = new NotificationDto();
		when(notificationService.findAllByUserId()).thenReturn(notificationDto);

		//when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/v1/notifications")
				.header("Monew-Request-User-Id", 1L)
		);

		//then
		perform.andExpect(jsonPath("$.status").value(equalTo(BAD_REQUEST.value())))
			.andExpect(jsonPath("$.code").value(equalTo(notificationInvalidException.toString())))
			.andExpect(jsonPath("$.message").value(equalTo(notificationInvalidException.getMessage())));
	}
}
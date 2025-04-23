package com.sprint.part3.sb01_monew_team6.controller;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;
import com.sprint.part3.sb01_monew_team6.service.NotificationService;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

	@MockitoBean
	private NotificationService notificationService;

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
		Long userId = 1L;
		Instant createdAt = Instant.parse("2025-04-22T00:00:00Z");

		NotificationDto notificationDto = new NotificationDto(
			1L,
			createdAt,
			Instant.now(),
			false,
			userId,
			"hello",
			ResourceType.COMMENT,
			1L
		);
		PageResponse<NotificationDto> pageResponse = new PageResponse<>(
			List.of(notificationDto),
			createdAt,
			createdAt,
			50,
			false,
			1L
		);
		when(notificationService.findAllByUserId(eq(userId), any(), any())).thenReturn(pageResponse);

		//when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/v1/notifications")
				.header("Monew-Request-User-Id", 1L)
		);

		//then
		perform.andExpect(status().isOk())
			.andExpect(jsonPath("$.contents.length()").value(equalTo(1)))
			.andExpect(jsonPath("$.contents[0].userId").value(equalTo(1)))
			.andExpect(jsonPath("$.nextCursor").value(equalTo(createdAt.toString())))
			.andExpect(jsonPath("$.hasNext").value(equalTo(false)))
			.andExpect(jsonPath("$.size").value(equalTo(50)))
			.andExpect(jsonPath("$.totalElements").value(equalTo(1)));
	}

	@Test
	@DisplayName("updateAllByUserId 정상 호출 시 정상 상태 코드 반환")
	public void respondOkStatusWhenUpdateAllByUserIdSucceed() throws Exception {

		//given & when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/v1/notifications")
				.header("Monew-Request-User-Id", 1L)
		);

		//then
		perform.andExpect(status().isOk());
	}
}
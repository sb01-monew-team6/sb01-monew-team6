package com.sprint.part3.sb01_monew_team6;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.GlobalExceptionHandler;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestException;
import com.sprint.part3.sb01_monew_team6.stub.TestController;

@WebMvcTest(controllers = TestController.class)
@Import(GlobalExceptionHandler.class)
class TestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("사용자 예외가 발생하여 응답으로 예외 정보를 내려준다")
	public void throwUserException() throws Exception {
		//given

		//when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/v1/test/user")
		);

		//then
		ErrorCode userLoginFailedException = USER_LOGIN_FAILED_EXCEPTION;

		perform.andExpect(jsonPath("$.status").value(equalTo(HttpStatus.UNAUTHORIZED.value())))
			.andExpect(jsonPath("$.code").value(equalTo(userLoginFailedException.toString())))
			.andExpect(jsonPath("$.message").value(equalTo(userLoginFailedException.getMessage())));
	}

	@Test
	@DisplayName("관심사를_찾을_수_없는_예외가_발생")
	void InterestExceptionSuccess() throws Exception {

		//given
		String requestPath = "/api/v1/test/interest";
		ErrorCode expectedErrorCode = INTEREST_INVALID_EXCEPTION;
		//when
		ResultActions perform = mockMvc.perform(
			get(requestPath)
				.accept(MediaType.APPLICATION_JSON)
		);
		//then
		perform
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("$.code", is(expectedErrorCode.toString())))
			.andExpect(jsonPath("$.message", is(expectedErrorCode.getMessage())))
			.andExpect((jsonPath("$.exceptionType", is(InterestException.class.getSimpleName()))))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	@DisplayName("뉴스를 찾을 수 없는 예외가 정상적으로 작동")
	void newsExceptionSuccess() throws Exception {
		//given
		String path = "/api/v1/test/news";
		//when
		ResultActions perform = mockMvc.perform(
			MockMvcRequestBuilders.get(path)
		);
		//then
		ErrorCode newsNotFoundException = NEWS_INVALID_EXCEPTION;
		perform
			.andExpect(jsonPath("$.code").value(equalTo(newsNotFoundException.toString())))
			.andExpect(jsonPath("$.message").value(equalTo(newsNotFoundException.getMessage())));
	}
}

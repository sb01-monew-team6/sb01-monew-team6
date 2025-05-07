package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.MonewRequestUserInterceptor;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserNicknameUpdateRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.user.EmailAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.user.LoginFailedException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@MockBean(UserRepository.class)
@MockBean(MonewRequestUserInterceptor.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	// 활성 프로파일 로깅
	@Autowired
	Environment environment;

	@BeforeEach
	void printActiveProfiles() {
		System.out.println("[DEBUG] Active profiles in UserControllerTest: " + Arrays.toString(environment.getActiveProfiles()));
	}

	// --- 회원가입 테스트 ---
	@Test
	@DisplayName("성공: POST /api/users - 유효한 정보로 회원가입 요청 시 201 Created와 UserDto 반환")
	// @WithMockUser // 회원가입은 인증 없이 가능해야 함
	void registerUser_withValidRequest_returns201AndUserDto() throws Exception {
		// given
		UserRegisterRequest requestDto = new UserRegisterRequest("test@example.com", "tester", "password123");
		String requestJson = objectMapper.writeValueAsString(requestDto);

		User returnedUser = User.builder()
				.email(requestDto.email())
				.nickname(requestDto.nickname())
				.password("encodedPassword")
				.build();
		ReflectionTestUtils.setField(returnedUser, "id", 1L);
		ReflectionTestUtils.setField(returnedUser, "createdAt", Instant.now());

		UserDto expectedResponseDto = UserDto.fromEntity(returnedUser);

		when(userService.registerUser(any(UserRegisterRequest.class))).thenReturn(returnedUser);

		// when & then
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", is(expectedResponseDto.id().intValue())))
				.andExpect(jsonPath("$.email", is(expectedResponseDto.email())))
				.andExpect(jsonPath("$.nickname", is(expectedResponseDto.nickname())))
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.createdAt").isNotEmpty());

		verify(userService).registerUser(any(UserRegisterRequest.class));
	}

	@Test
	@DisplayName("실패: POST /api/users - 이미 존재하는 이메일로 회원가입 시 409 Conflict 반환")
	void registerUser_whenEmailExists_shouldReturnConflict() throws Exception {
		// given
		UserRegisterRequest requestDto = new UserRegisterRequest("exists@example.com", "someUser", "password123");
		String requestJson = objectMapper.writeValueAsString(requestDto);

		when(userService.registerUser(any(UserRegisterRequest.class)))
				.thenThrow(new EmailAlreadyExistsException(requestDto.email()));

		// when & then
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isConflict());

		verify(userService).registerUser(any(UserRegisterRequest.class));
	}

	@Test
	@DisplayName("실패: POST /api/users - 유효하지 않은 요청 데이터(빈 닉네임) 시 400 Bad Request 반환")
	void registerUser_withInvalidNickname_shouldReturnBadRequest() throws Exception {
		// given
		UserRegisterRequest requestDto = new UserRegisterRequest("valid@email.com", "", "password123");
		String requestJson = objectMapper.writeValueAsString(requestDto);

		// when & then
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isBadRequest());

		verify(userService, never()).registerUser(any(UserRegisterRequest.class));
	}

	// --- 로그인 테스트 ---
	@Test
	@DisplayName("성공: POST /api/users/login - 유효한 정보로 로그인 요청 시 200 OK와 UserDto 반환")
	void login_withValidCredentials_returnsOkAndUserDto() throws Exception {
		// given
		UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "password123");
		String requestJson = objectMapper.writeValueAsString(loginRequest);
		User loggedInUser = User.builder().email(loginRequest.email()).nickname("tester").password("encodedPassword").build();
		ReflectionTestUtils.setField(loggedInUser, "id", 1L);
		ReflectionTestUtils.setField(loggedInUser, "createdAt", Instant.now());
		UserDto expectedDto = UserDto.fromEntity(loggedInUser);

		when(userService.login(any(UserLoginRequest.class))).thenReturn(loggedInUser);

		// when & then
		mockMvc.perform(post("/api/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.email", is(expectedDto.email())))
				.andExpect(jsonPath("$.nickname", is(expectedDto.nickname())))
				.andExpect(jsonPath("$.createdAt").isNotEmpty());

		verify(userService).login(any(UserLoginRequest.class));
	}

	@Test
	@DisplayName("실패: POST /api/users/login - 잘못된 비밀번호로 로그인 요청 시 401 Unauthorized 반환")
	void login_withWrongPassword_returnsUnauthorized() throws Exception {
		// given
		UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "wrongPassword");
		String requestJson = objectMapper.writeValueAsString(loginRequest);

		when(userService.login(any(UserLoginRequest.class)))
				.thenThrow(new LoginFailedException());

		// when & then
		mockMvc.perform(post("/api/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isUnauthorized());

		verify(userService).login(any(UserLoginRequest.class));
	}

	@Test
	@DisplayName("실패: POST /api/users/login - 존재하지 않는 이메일로 로그인 요청 시 401 Unauthorized 반환")
	void login_whenUserNotFound_shouldReturnUnauthorized() throws Exception {
		// given
		UserLoginRequest loginRequest = new UserLoginRequest("nobody@example.com", "password123");
		String requestJson = objectMapper.writeValueAsString(loginRequest);

		when(userService.login(any(UserLoginRequest.class)))
				.thenThrow(new LoginFailedException());

		// when & then
		mockMvc.perform(post("/api/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isUnauthorized());

		verify(userService).login(any(UserLoginRequest.class));
	}

	@Test
	@DisplayName("실패: POST /api/users/login - 논리 삭제된 사용자로 로그인 요청 시 401 Unauthorized 반환")
	void login_whenUserIsDeleted_shouldReturnUnauthorized() throws Exception {
		// given
		UserLoginRequest loginRequest = new UserLoginRequest("deleted@example.com", "password123");
		String requestJson = objectMapper.writeValueAsString(loginRequest);

		when(userService.login(any(UserLoginRequest.class)))
				.thenThrow(new LoginFailedException());

		// when & then
		mockMvc.perform(post("/api/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isUnauthorized());

		verify(userService).login(any(UserLoginRequest.class));
	}

	// --- 닉네임 수정 테스트 ---
	@Test
	@DisplayName("성공: PATCH /api/users/{userId} - 유효한 요청 시 200 OK 와 UserDto 반환")
	@WithMockUser // 수정 권한 필요 가정
	void updateNickname_withValidRequest_shouldReturnOkAndUserDto() throws Exception {
		// given
		Long userId = 1L;
		String newNickname = "newNickname";
		UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest(newNickname);
		String requestJson = objectMapper.writeValueAsString(requestDto);
		User updatedUser = User.builder().email("test@example.com").nickname(newNickname).password("encodedPassword").build();
		ReflectionTestUtils.setField(updatedUser, "id", userId);
		ReflectionTestUtils.setField(updatedUser, "createdAt", Instant.now());
		UserDto expectedResponseDto = UserDto.fromEntity(updatedUser);

		when(userService.updateNickname(eq(userId), eq(newNickname))).thenReturn(updatedUser);

		// when & then
		mockMvc.perform(patch("/api/users/{userId}", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", is(userId.intValue())))
				.andExpect(jsonPath("$.nickname", is(newNickname)))
				.andExpect(jsonPath("$.email", is(updatedUser.getEmail())));

		verify(userService).updateNickname(eq(userId), eq(newNickname));
	}

	@Test
	@DisplayName("실패: PATCH /api/users/{userId} - 존재하지 않는 사용자의 닉네임 수정 시 404 Not Found 반환")
	@WithMockUser // 인증 필요
	void updateNickname_whenUserNotFound_shouldReturnNotFound() throws Exception {
		// given
		Long nonExistentUserId = 999L;
		String newNickname = "someNickname";
		UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest(newNickname);
		String requestJson = objectMapper.writeValueAsString(requestDto);

		when(userService.updateNickname(eq(nonExistentUserId), eq(newNickname)))
				.thenThrow(new UserNotFoundException(nonExistentUserId));

		// when & then
		mockMvc.perform(patch("/api/users/{userId}", nonExistentUserId)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isNotFound());

		verify(userService).updateNickname(eq(nonExistentUserId), eq(newNickname));
	}

	@Test
	@DisplayName("실패: PATCH /api/users/{userId} - 유효하지 않은 닉네임 요청 시 400 Bad Request 반환")
	@WithMockUser // 인증 필요
	void updateNickname_withInvalidNickname_shouldReturnBadRequest() throws Exception {
		// given
		Long userId = 1L;
		UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest("");
		String requestJson = objectMapper.writeValueAsString(requestDto);

		// when & then
		mockMvc.perform(patch("/api/users/{userId}", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.with(csrf()))
				.andExpect(status().isBadRequest());

		verify(userService, never()).updateNickname(anyLong(), anyString());
	}

	// --- 논리 삭제 테스트 ---
	@Test
	@DisplayName("성공: DELETE /api/users/{userId} - 존재하는 사용자 논리 삭제 성공 시 204 No Content 반환")
	@WithMockUser
	void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
		// given
		Long userId = 1L;
		doNothing().when(userService).deleteUser(userId);

		// when & then
		mockMvc.perform(delete("/api/users/{userId}", userId)
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(userService).deleteUser(userId);
	}

	@Test
	@DisplayName("실패: DELETE /api/users/{userId} - 존재하지 않는 사용자 논리 삭제 시 404 Not Found 반환")
	@WithMockUser
	void deleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
		// given
		Long nonExistentUserId = 999L;
		doThrow(new UserNotFoundException(nonExistentUserId)).when(userService).deleteUser(nonExistentUserId);

		// when & then
		mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
						.with(csrf()))
				.andExpect(status().isNotFound());

		verify(userService).deleteUser(nonExistentUserId);
	}

	@Test
	@DisplayName("DELETE /api/users/{userId}/hard - 관리자가 사용자 물리 삭제 성공 시 204 No Content 반환")
	@WithMockUser(roles = "ADMIN")
	void hardDeleteUser_whenAdminAndUserExists_shouldReturnNoContent() throws Exception {
		// given
		Long userId = 1L;
		doNothing().when(userService).hardDeleteUser(userId);

		// when & then
		mockMvc.perform(delete("/api/users/{userId}/hard", userId)
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(userService).hardDeleteUser(userId);
	}

	@Test
	@DisplayName("DELETE /api/users/{userId}/hard - 존재하지 않는 사용자 삭제 시 404 Not Found 반환")
	@WithMockUser(roles = "ADMIN")
	void hardDeleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
		// given
		Long nonExistentUserId = 999L;
		doThrow(new UserNotFoundException(nonExistentUserId))
				.when(userService).hardDeleteUser(nonExistentUserId);

		// when & then
		mockMvc.perform(delete("/api/users/{userId}/hard", nonExistentUserId)
						.with(csrf()))
				.andExpect(status().isNotFound());

		verify(userService).hardDeleteUser(nonExistentUserId);
	}

	@Test
	@DisplayName("실패: DELETE /api/users/{userId}/hard - 관리자가 아닌 사용자가 물리 삭제 시 403 Forbidden 반환")
	@WithMockUser(roles = "USER")
	void hardDeleteUser_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
		// given
		Long userId = 1L;

		// when & then
		mockMvc.perform(delete("/api/users/{userId}/hard", userId)
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(userService, never()).hardDeleteUser(userId);
	}

	@Test
	@DisplayName("실패: DELETE /api/users/{userId}/hard - 인증되지 않은 사용자가 물리 삭제 시 403 Forbidden 반환") // <<<--- 기대값 403으로 변경
	void hardDeleteUser_whenUnauthenticated_shouldReturnForbidden() throws Exception { // <<<--- 메서드 이름 명확화 (선택)
		// given
		Long userId = 1L;

		// when & then: @WithMockUser 없이 요청
		mockMvc.perform(delete("/api/users/{userId}/hard", userId)
						.with(csrf()))
				.andExpect(status().isForbidden()); // <<<--- 403 Forbidden 상태 코드를 기대 (AnonymousUser에 의한 AccessDenied)

		// then: 인증/인가 실패로 서비스 메소드는 호출되지 않아야 함
		verify(userService, never()).hardDeleteUser(userId);
	}
}

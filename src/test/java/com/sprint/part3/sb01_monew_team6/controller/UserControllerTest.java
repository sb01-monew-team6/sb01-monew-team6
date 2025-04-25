package com.sprint.part3.sb01_monew_team6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserNicknameUpdateRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.user.EmailAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.user.LoginFailedException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
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
import static org.assertj.core.api.Assertions.*;


@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
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
  @WithMockUser
  void registerUser_withValidRequest_returns201AndUserDto() throws Exception {
    // given
    UserRegisterRequest requestDto = new UserRegisterRequest("test@example.com", "tester", "password123");
    String requestJson = objectMapper.writeValueAsString(requestDto);

    User returnedUser = User.builder()
        .email(requestDto.email())
        .nickname(requestDto.nickname())
        .password("encodedPassword")
        .build();
    // Mock 객체에 ID와 생성 시각 설정
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
        .andExpect(status().isCreated()) // 201 Created
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

    // Mockito 설정: userService.registerUser 호출 시 EmailAlreadyExistsException 발생
    when(userService.registerUser(any(UserRegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException(requestDto.email())); // 예외 발생 Mocking

    // when & then: MockMvc로 POST 요청 및 409 상태 코드 검증
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isConflict());

    // then: 서비스 메소드 호출 검증
    verify(userService).registerUser(any(UserRegisterRequest.class));
  }

  @Test
  @DisplayName("실패: POST /api/users - 유효하지 않은 요청 데이터(빈 닉네임) 시 400 Bad Request 반환")
  void registerUser_withInvalidNickname_shouldReturnBadRequest() throws Exception {
    // given: 유효하지 않은 DTO (닉네임 비어 있음)
    UserRegisterRequest requestDto = new UserRegisterRequest("valid@email.com", "", "password123"); // @NotBlank 위반
    String requestJson = objectMapper.writeValueAsString(requestDto);

    // when & then: @Valid 어노테이션에 의해 400 Bad Request 기대
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isBadRequest());

    // then: 서비스 메소드는 호출되지 않아야 함
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
        .andExpect(status().isOk()) // 200 OK
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

    // LoginFailedException은 내부적으로 401 상태 코드를 가짐
    when(userService.login(any(UserLoginRequest.class)))
        .thenThrow(new LoginFailedException());

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isUnauthorized()); // 401 기대

    verify(userService).login(any(UserLoginRequest.class));
  }

  @Test
  @DisplayName("실패: POST /api/users/login - 존재하지 않는 이메일로 로그인 요청 시 401 Unauthorized 반환")
  void login_whenUserNotFound_shouldReturnUnauthorized() throws Exception {
    // given
    UserLoginRequest loginRequest = new UserLoginRequest("nobody@example.com", "password123");
    String requestJson = objectMapper.writeValueAsString(loginRequest);

    when(userService.login(any(UserLoginRequest.class)))
        .thenThrow(new LoginFailedException()); // UserServiceImpl 에서 동일한 예외 사용

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isUnauthorized()); // 401 기대

    verify(userService).login(any(UserLoginRequest.class));
  }

  @Test
  @DisplayName("실패: POST /api/users/login - 논리 삭제된 사용자로 로그인 요청 시 401 Unauthorized 반환")
  void login_whenUserIsDeleted_shouldReturnUnauthorized() throws Exception {
    // given
    UserLoginRequest loginRequest = new UserLoginRequest("deleted@example.com", "password123");
    String requestJson = objectMapper.writeValueAsString(loginRequest);

    when(userService.login(any(UserLoginRequest.class)))
        .thenThrow(new LoginFailedException()); // UserServiceImpl 에서 동일한 예외 사용

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isUnauthorized()); // 401 기대

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

    // UserNotFoundException은 내부적으로 404 상태 코드 가짐
    when(userService.updateNickname(eq(nonExistentUserId), eq(newNickname)))
        .thenThrow(new UserNotFoundException(nonExistentUserId));

    // when & then
    mockMvc.perform(patch("/api/users/{userId}", nonExistentUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isNotFound()); // 404 기대

    verify(userService).updateNickname(eq(nonExistentUserId), eq(newNickname));
  }

  @Test
  @DisplayName("실패: PATCH /api/users/{userId} - 유효하지 않은 닉네임 요청 시 400 Bad Request 반환")
  @WithMockUser // 인증 필요
  void updateNickname_withInvalidNickname_shouldReturnBadRequest() throws Exception {
    // given
    Long userId = 1L;
    UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest(""); // @NotBlank 위반
    String requestJson = objectMapper.writeValueAsString(requestDto);

    // when & then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isBadRequest()); // 400 기대

    verify(userService, never()).updateNickname(anyLong(), anyString());
  }

  // --- 논리 삭제 테스트 ---
  @Test
  @DisplayName("성공: DELETE /api/users/{userId} - 존재하는 사용자 논리 삭제 성공 시 204 No Content 반환")
  @WithMockUser // 삭제 권한 필요 가정
  void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
    // given
    Long userId = 1L;
    // Mockito 설정: userService.deleteUser(userId)가 호출되어도 아무 일도 일어나지 않음 (void)
    // doNothing()은 기본 동작이므로 명시적으로 필요 없을 수 있음
    // doNothing().when(userService).deleteUser(userId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .with(csrf()))
        .andExpect(status().isNoContent()); // 204 No Content 기대

    verify(userService).deleteUser(userId);
  }

  @Test
  @DisplayName("실패: DELETE /api/users/{userId} - 존재하지 않는 사용자 논리 삭제 시 404 Not Found 반환")
  @WithMockUser // 인증 필요
  void deleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
    // given
    Long nonExistentUserId = 999L;

    // UserNotFoundException은 내부적으로 404 상태 코드 가짐
    doThrow(new UserNotFoundException(nonExistentUserId)).when(userService).deleteUser(nonExistentUserId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
            .with(csrf()))
        .andExpect(status().isNotFound()); // 404 기대

    verify(userService).deleteUser(nonExistentUserId);
  }

  @Test
  @DisplayName("DELETE /api/users/{userId}/hard - 관리자가 사용자 물리 삭제 성공 시 204 No Content 반환")
  @WithMockUser(roles = "ADMIN") // <<<--- 관리자 권한으로 테스트 실행
  void hardDeleteUser_whenAdminAndUserExists_shouldReturnNoContent() throws Exception {
    // given: 삭제할 사용자 ID
    Long userId = 1L;

    // Mockito 설정: userService.hardDeleteUser(userId)가 호출되면 아무 일도 일어나지 않음 (void)
    // void 메소드에 대한 명시적 Mocking은 verify로 충분하지만, doNothing() 사용 가능
    doNothing().when(userService).hardDeleteUser(userId);

    // when & then //
    mockMvc.perform(delete("/api/users/{userId}/hard", userId) // DELETE /api/users/1/hard 요청
            .with(csrf())) // CSRF 토큰
        .andExpect(status().isNoContent());

    // then: 서비스 메소드 호출 검증
    verify(userService).hardDeleteUser(userId); // 서비스 메소드가 userId로 호출되었는지 확인
  }

  @Test
  @DisplayName("DELETE /api/users/{userId}/hard - 존재하지 않는 사용자 삭제 시 404 Not Found 반환")
  @WithMockUser(roles = "ADMIN")
  void hardDeleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
    // given: 존재하지 않는 사용자 ID
    Long nonExistentUserId = 999L;

    // Mockito 설정: userService.hardDeleteUser 호출 시 UserNotFoundException 발생하도록 설정
    // UserNotFoundException은 내부적으로 HttpStatus.NOT_FOUND 를 가짐
    doThrow(new UserNotFoundException(nonExistentUserId))
        .when(userService).hardDeleteUser(nonExistentUserId);

    // when & then: MockMvc로 DELETE 요청 및 404 상태 코드 검증
    mockMvc.perform(delete("/api/users/{userId}/hard", nonExistentUserId)
            .with(csrf()))
        .andExpect(status().isNotFound());

    // then: 서비스 메소드 호출 검증
    verify(userService).hardDeleteUser(nonExistentUserId);
  }

  @Test
  @DisplayName("실패: DELETE /api/users/{userId}/hard - 관리자가 아닌 사용자가 물리 삭제 시 403 Forbidden 반환")
  @WithMockUser(roles = "USER") // <<<--- 'ADMIN'이 아닌 다른 역할 (예: 'USER') 또는 역할 없이 호출
    // 또는 @WithMockUser // 역할 없이 호출해도 ADMIN이 아니므로 403 예상
  void hardDeleteUser_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
    // given: 삭제할 사용자 ID (실제 삭제 로직은 호출되지 않음)
    Long userId = 1L;

    // when & then: MockMvc로 DELETE 요청 및 403 상태 코드 검증
    mockMvc.perform(delete("/api/users/{userId}/hard", userId)
            .with(csrf()))
        .andExpect(status().isForbidden()); // <<<--- 403 Forbidden 상태 코드를 기대

    // then: 권한 부족으로 서비스 메소드는 호출되지 않아야 함
    verify(userService, never()).hardDeleteUser(userId);
  }

  @Test
  @DisplayName("실패: DELETE /api/users/{userId}/hard - 인증되지 않은 사용자가 물리 삭제 시 401 Unauthorized 반환")
  void hardDeleteUser_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
    // given: 삭제할 사용자 ID (실제 삭제 로직은 호출되지 않음)
    Long userId = 1L;

    // when & then: @WithMockUser 없이 요청 & 401 상태 코드 검증
    mockMvc.perform(delete("/api/users/{userId}/hard", userId)
            .with(csrf())) // CSRF 토큰은 필요할 수 있음
        .andExpect(status().isUnauthorized()); // <<<--- 401 Unauthorized 상태 코드를 기대

    // then: 인증 실패로 서비스 메소드는 호출되지 않아야 함
    verify(userService, never()).hardDeleteUser(userId);
  }
}
package com.sprint.part3.sb01_monew_team6.controller; // 패키지 확인

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.service.UserService;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserNicknameUpdateRequest;
import com.sprint.part3.sb01_monew_team6.exception.user.EmailAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.user.LoginFailedException;
import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import java.time.Instant;
import java.util.Arrays; // <<<--- Arrays 임포트 추가
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig;
import org.springframework.context.annotation.Import; // Import 임포트 예시
import org.springframework.security.authentication.BadCredentialsException;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(UserController.class
)
@ActiveProfiles("test")
@Import(SecurityConfig.class) // <<<--- SecurityConfig 임포트 유지 또는 추가 (WebMvcTest 환경에서 명시적 로딩)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  Environment environment;

  @BeforeEach
  void printActiveProfiles() {
    System.out.println("[DEBUG] Active profiles in UserControllerTest: " + Arrays.toString(environment.getActiveProfiles()));
  }


  @Test
  @DisplayName("POST /api/users - 가짜 인증 상태에서 회원가입 요청 시 201 Created와 UserDto 반환")
  @WithMockUser // 회원가입 테스트에도 일단 유지 (필수는 아님)
  void registerUser_withValidRequest_returns201AndUserDto() throws Exception {
    // given //
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

    // when & then: ...
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email", is(expectedResponseDto.email())))
        .andExpect(jsonPath("$.nickname", is(expectedResponseDto.nickname())))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.createdAt").isNotEmpty());

    // then:
    verify(userService).registerUser(any(UserRegisterRequest.class));
  }

  @Test
  @DisplayName("POST /api/users/login - 유효한 정보로 로그인 요청 시 200 OK와 UserDto 반환")
  @WithMockUser
  void login_withValidCredentials_returnsOkAndUserDto() throws Exception {
    // === given: 유효한 로그인 요청 및 Mock Service 설정 ===
    UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "password123");
    String requestJson = objectMapper.writeValueAsString(loginRequest);
    User loggedInUser = User.builder()
        .email(loginRequest.email())
        .nickname("tester")
        .password("encodedPassword")
        .build();
    ReflectionTestUtils.setField(loggedInUser, "id", 1L);
    ReflectionTestUtils.setField(loggedInUser, "createdAt", Instant.now());
    UserDto expectedDto = UserDto.fromEntity(loggedInUser);
    when(userService.login(any(UserLoginRequest.class))).thenReturn(loggedInUser);

    // === when & then: MockMvc로 POST 요청 수행 및 결과 검증 ===
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email", is(expectedDto.email())))
        .andExpect(jsonPath("$.nickname", is(expectedDto.nickname())))
        .andExpect(jsonPath("$.createdAt").isNotEmpty());
    // === then: 서비스 메소드 호출 검증 ===
    verify(userService).login(any(UserLoginRequest.class));
  }

  @Test
  @DisplayName("POST /api/users/login - 잘못된 비밀번호로 로그인 요청 시 401 Unauthorized 반환")
  void login_withWrongPassword_returnsUnauthorized() throws Exception {
    // === given ===
    UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "wrongPassword");
    String requestJson = objectMapper.writeValueAsString(loginRequest);

    when(userService.login(any(UserLoginRequest.class)))
        .thenThrow(new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다."));

    // === when & then ===
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isUnauthorized()); // <-- 401 기대

    // === then ===
    verify(userService).login(any(UserLoginRequest.class));
  }
  @Test
  @DisplayName("PATCH /api/users/{userId} - 유효한 요청 시 200 OK 와 UserDto 반환")
  @WithMockUser // <<<--- 수정 권한이 필요한 API이므로 인증된 사용자 가정
  void updateNickname_withValidRequest_shouldReturnOkAndUserDto() throws Exception {
    // given: 요청 데이터 및 Mock Service 설정
    Long userId = 1L;
    String newNickname = "newNickname";
    UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest(newNickname);
    String requestJson = objectMapper.writeValueAsString(requestDto);

    User updatedUser = User.builder()
        .email("test@example.com") // 기존 이메일 유지 가정
        .nickname(newNickname)    // 새 닉네임으로 변경됨
        .password("encodedPassword")
        .build();
    ReflectionTestUtils.setField(updatedUser, "id", userId); // ID 설정
    ReflectionTestUtils.setField(updatedUser, "createdAt", Instant.now()); // createdAt 설정

    UserDto expectedResponseDto = UserDto.fromEntity(updatedUser);

    // Mockito 설정: userService.updateNickname 호출 시 updatedUser 반환하도록 설정
    when(userService.updateNickname(eq(userId), eq(newNickname))).thenReturn(updatedUser);

    // when & then: MockMvc로 PATCH 요청 및 결과 검증 (아직 Controller 없음 -> 404 예상)
    mockMvc.perform(patch("/api/users/{userId}", userId) // PATCH /api/users/1 요청
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf())) // CSRF 토큰 추가 (Security 설정에 따라 필요)
        .andExpect(status().isOk()) // 200 OK 상태 코드 확인
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(userId.intValue()))) // ID 검증 (Long -> int 변환 주의)
        .andExpect(jsonPath("$.nickname", is(newNickname))) // 변경된 닉네임 확인
        .andExpect(jsonPath("$.email", is(updatedUser.getEmail()))); // 이메일은 그대로인지 확인

    // then: 서비스 메소드 호출 검증
    verify(userService).updateNickname(eq(userId), eq(newNickname)); // 정확한 인자로 호출되었는지 확인
  }
  @Test
  @DisplayName("실패: 존재하지 않는 사용자의 닉네임 수정 시 404 Not Found 반환")
  @WithMockUser // 인증된 사용자 가정
  void updateNickname_whenUserNotFound_shouldReturnNotFound() throws Exception {
    // given: 존재하지 않는 사용자 ID 및 요청 데이터
    Long nonExistentUserId = 999L;
    String newNickname = "someNickname";
    UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest(newNickname);
    String requestJson = objectMapper.writeValueAsString(requestDto);

    // Mockito 설정: userService.updateNickname 호출 시 UserNotFoundException 발생하도록 설정
    // new UserNotFoundException(nonExistentUserId)는 내부적으로 HttpStatus.NOT_FOUND 와 ErrorCode.USER_NOT_FOUND 를 가짐
    when(userService.updateNickname(eq(nonExistentUserId), eq(newNickname)))
        .thenThrow(new UserNotFoundException(nonExistentUserId));

    // when & then: MockMvc로 PATCH 요청 및 404 상태 코드 검증
    mockMvc.perform(patch("/api/users/{userId}", nonExistentUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isNotFound()); // <<<--- 404 Not Found 상태 코드를 기대

    // then: 서비스 메소드 호출 검증
    verify(userService).updateNickname(eq(nonExistentUserId), eq(newNickname));
  }
  @Test
  @DisplayName("TDD (Red): PATCH /api/users/{userId} - 유효하지 않은 닉네임 요청 시 400 Bad Request 반환")
  @WithMockUser // 인증된 사용자 가정
  void updateNickname_withInvalidNickname_shouldReturnBadRequest() throws Exception {
    // given: 유효하지 않은 요청 DTO (닉네임이 비어 있음)
    Long userId = 1L;
    UserNicknameUpdateRequest requestDto = new UserNicknameUpdateRequest(""); // @NotBlank 위반
    String requestJson = objectMapper.writeValueAsString(requestDto);

    // when & then: MockMvc로 PATCH 요청 및 400 상태 코드 검증
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isBadRequest()); // <<<--- 400 Bad Request 상태 코드를 기대

    // then: 유효성 검증 실패 시 Service 메소드는 호출되지 않아야 함
    verify(userService, never()).updateNickname(anyLong(), anyString());
  }
  @Test
  @DisplayName("DELETE /api/users/{userId} - 존재하는 사용자 논리 삭제 성공 시 204 No Content 반환")
  @WithMockUser // <<<--- 삭제 권한이 필요한 API이므로 인증된 사용자 가정
  void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
    // given: 삭제할 사용자 ID
    Long userId = 1L;
    // when & then: MockMvc로 DELETE 요청 수행 및 결과 검증 (아직 Controller 없음 -> 404 예상)
    mockMvc.perform(delete("/api/users/{userId}", userId) // DELETE 요청
            .with(csrf())) // CSRF 토큰 추가
        .andExpect(status().isNoContent()); // <<<--- 204 No Content 상태 코드를 기대

    // then: 서비스 메소드 호출 검증
    verify(userService).deleteUser(userId); // deleteUser가 userId로 호출되었는지 확인
  }

  @Test
  @DisplayName("실패: 존재하지 않는 사용자 논리 삭제 시 404 Not Found 반환")
  @WithMockUser // 인증된 사용자 가정
  void deleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
    // given: 존재하지 않는 사용자 ID
    Long nonExistentUserId = 999L;
    doThrow(new UserNotFoundException(nonExistentUserId)).when(userService).deleteUser(nonExistentUserId);
    // when & then: MockMvc로 DELETE 요청 및 404 상태 코드 검증
    mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
            .with(csrf()))
        .andExpect(status().isNotFound());

    // then: 서비스 메소드 호출 검증
    verify(userService).deleteUser(nonExistentUserId);
  }
  @Test
  @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 409 Conflict 반환")
  void registerUser_whenEmailExists_shouldReturnConflict() throws Exception {
    // given: 유효한 요청 DTO 준비
    UserRegisterRequest requestDto = new UserRegisterRequest("exists@example.com", "someUser", "password123");
    String requestJson = objectMapper.writeValueAsString(requestDto);

    when(userService.registerUser(any(UserRegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException(requestDto.email()));

    // when & then: MockMvc로 POST 요청 및 409 상태 코드 검증
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf())) // CSRF 필요시 유지
        .andExpect(status().isConflict()); // <<<--- 409 Conflict 상태 코드를 기대

    // then: 서비스 메소드 호출 검증
    verify(userService).registerUser(any(UserRegisterRequest.class));
  }


  @Test
  @DisplayName("실패: 존재하지 않는 이메일로 로그인 요청 시 401 Unauthorized 반환")
  void login_whenUserNotFound_shouldReturnUnauthorized() throws Exception {
    // given: 존재하지 않는 이메일로 로그인 요청 데이터
    UserLoginRequest loginRequest = new UserLoginRequest("nobody@example.com", "password123");
    String requestJson = objectMapper.writeValueAsString(loginRequest);

    // Mockito 설정: userService.login 호출 시 LoginFailedException 발생하도록 설정
    // UserServiceImpl의 login 메소드는 사용자를 못찾을 경우 LoginFailedException을 던짐
    when(userService.login(any(UserLoginRequest.class)))
        .thenThrow(new LoginFailedException()); // LoginFailedException은 내부적으로 HttpStatus.UNAUTHORIZED를 가짐

    // when & then: MockMvc로 POST 요청 및 401 상태 코드 검증
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isUnauthorized()); // <<<--- 401 Unauthorized 상태 코드를 기대

    // then: 서비스 메소드 호출 검증
    verify(userService).login(any(UserLoginRequest.class));
  }

  @Test
  @DisplayName("실패: 논리 삭제된 사용자로 로그인 요청 시 401 Unauthorized 반환")
  void login_whenUserIsDeleted_shouldReturnUnauthorized() throws Exception {
    // given: 논리 삭제된 사용자의 정보로 로그인 요청
    UserLoginRequest loginRequest = new UserLoginRequest("deleted@example.com", "password123");
    String requestJson = objectMapper.writeValueAsString(loginRequest);

    // Mockito 설정: userService.login 호출 시 LoginFailedException 발생하도록 설정
    // UserServiceImpl의 login 메소드는 삭제된 사용자 확인 시 LoginFailedException을 던짐
    when(userService.login(any(UserLoginRequest.class)))
        .thenThrow(new LoginFailedException()); // 기본 LoginFailedException 또는 특정 메시지 포함 가능

    // when & then: MockMvc로 POST 요청 및 401 상태 코드 검증
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andExpect(status().isUnauthorized()); // <<<--- 401 Unauthorized 상태 코드를 기대

    // then: 서비스 메소드 호출 검증
    verify(userService).login(any(UserLoginRequest.class));
  }
}
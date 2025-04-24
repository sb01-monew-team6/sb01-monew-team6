package com.sprint.part3.sb01_monew_team6.controller; // 패키지 확인

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest; // UserLoginRequest 사용 시 해당 DTO 임포트
import com.sprint.part3.sb01_monew_team6.service.UserService;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.entity.User;
import org.junit.jupiter.api.BeforeEach; // <<<--- BeforeEach 임포트 추가
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.env.Environment; // <<<--- Environment 임포트 추가
import org.springframework.http.MediaType;
import java.time.Instant;
import java.util.Arrays; // <<<--- Arrays 임포트 추가
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
// SecurityConfig 임포트 (만약 @Import 사용 시)
import com.sprint.part3.sb01_monew_team6.config.SecurityConfig; // SecurityConfig 임포트 예시
import org.springframework.context.annotation.Import; // Import 임포트 예시
import org.springframework.security.authentication.BadCredentialsException;
// 자동 설정 제외 (만약 사용 시)
// import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
// import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// 필요한 import 문들 추가
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

  // --- 활성 프로파일 로깅 추가 ---
  @Autowired
  Environment environment;

  @BeforeEach // 각 테스트 메서드 실행 전에 호출
  void printActiveProfiles() {
    System.out.println("[DEBUG] Active profiles in UserControllerTest: " + Arrays.toString(environment.getActiveProfiles()));
  }


  @Test
  @DisplayName("POST /api/users - 가짜 인증 상태에서 회원가입 요청 시 201 Created와 UserDto 반환")
  @WithMockUser // 회원가입 테스트에도 일단 유지 (필수는 아님)
  void registerUser_withValidRequest_returns201AndUserDto() throws Exception {
    // given: ...
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
  @WithMockUser // <<<--- 로그인 테스트에도 @WithMockUser 추가 (진단 목적)
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
    // @WithMockUser // <<<--- 이 어노테이션을 제거하거나 주석 처리합니다.
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
}
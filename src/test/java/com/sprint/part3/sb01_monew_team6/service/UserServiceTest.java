package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.user.EmailAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.user.LoginFailedException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.UserServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  // --- 회원가입 테스트 ---
  @Test
  @DisplayName("성공: 신규 이메일로 회원가입 성공 시, 암호화된 비밀번호로 사용자 저장")
  void registerUser_whenEmailIsNew_savesUserWithEncodedPassword() {
    // given
    UserRegisterRequest request = new UserRegisterRequest("new@example.com", "newUser", "password123");
    String encodedPassword = "encodedPassword";
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);

    // when
    userService.registerUser(request);

    // then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getEmail()).isEqualTo(request.email());
    assertThat(savedUser.getNickname()).isEqualTo(request.nickname());
    assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
    assertThat(savedUser.isDeleted()).isFalse();
    assertThat(savedUser.getId()).isNull();

    verify(userRepository).existsByEmail(request.email());
    verify(passwordEncoder).encode(request.password());
  }

  @Test
  @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 EmailAlreadyExistsException 발생")
  void registerUser_whenEmailExists_throwsEmailAlreadyExistsException() {
    // given
    UserRegisterRequest request = new UserRegisterRequest("exists@example.com", "anotherUser", "password456");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    // when & then: 예외 타입 및 ErrorCode의 기본 메시지 검증
    assertThatThrownBy(() -> userService.registerUser(request))
        .isInstanceOf(EmailAlreadyExistsException.class)
        // .hasMessageContaining("이미 가입된 이메일입니다: " + request.email()); // <--- 이전 방식
        .hasMessage(EMAIL_ALREADY_EXISTS.getMessage()); // <<<--- 수정된 방식

    verify(userRepository, never()).save(any(User.class));
    verify(passwordEncoder, never()).encode(anyString());
  }

  // --- 로그인 테스트 ---
  @Test
  @DisplayName("성공: 유효한 정보로 로그인 시 User 객체 반환")
  void login_whenCredentialsAreValid_shouldReturnUser() {
    // given
    String rawPassword = "password123";
    String encodedPassword = "encodedPassword";
    String targetEmail = "test@example.com";
    UserLoginRequest request = new UserLoginRequest(targetEmail, rawPassword);
    User storedUser = User.builder().email(targetEmail).nickname("tester").password(encodedPassword).build();

    when(userRepository.findByEmail(targetEmail)).thenReturn(Optional.of(storedUser));
    when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

    // when
    User loggedInUser = userService.login(request);

    // then
    assertThat(loggedInUser).isNotNull();
    assertThat(loggedInUser.getEmail()).isEqualTo(targetEmail);
    assertThat(loggedInUser.getNickname()).isEqualTo("tester");
    assertThat(loggedInUser.isDeleted()).isFalse(); // 논리 삭제 안된 사용자

    verify(userRepository).findByEmail(targetEmail);
    verify(passwordEncoder).matches(rawPassword, encodedPassword);
  }

  @Test
  @DisplayName("실패: 존재하지 않는 이메일로 로그인 시 LoginFailedException 발생")
  void login_whenUserNotFound_shouldThrowLoginFailedException() {
    // given
    String nonExistentEmail = "nobody@example.com";
    UserLoginRequest request = new UserLoginRequest(nonExistentEmail, "password123");
    when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(LoginFailedException.class)
        .hasMessage(LOGIN_FAILED.getMessage()); // ErrorCode 메시지 검증

    verify(userRepository).findByEmail(nonExistentEmail);
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  @DisplayName("실패: 비밀번호 불일치 시 LoginFailedException 발생")
  void login_whenPasswordIsIncorrect_shouldThrowLoginFailedException() {
    // given
    String correctEmail = "test@example.com";
    String wrongPassword = "wrongPassword";
    String encodedPassword = "encodedPassword";
    UserLoginRequest request = new UserLoginRequest(correctEmail, wrongPassword);
    User storedUser = User.builder().email(correctEmail).nickname("tester").password(encodedPassword).build();

    when(userRepository.findByEmail(correctEmail)).thenReturn(Optional.of(storedUser));
    when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(LoginFailedException.class)
        .hasMessage(LOGIN_FAILED.getMessage()); // ErrorCode 메시지 검증

    verify(userRepository).findByEmail(correctEmail);
    verify(passwordEncoder).matches(wrongPassword, encodedPassword);
  }

  @Test
  @DisplayName("실패: 논리 삭제된 사용자로 로그인 시 LoginFailedException 발생")
  void login_whenUserIsDeleted_shouldThrowLoginFailedException() {
    // given
    String rawPassword = "password123";
    String encodedPassword = "encodedPassword";
    String targetEmail = "deleted@example.com";
    UserLoginRequest request = new UserLoginRequest(targetEmail, rawPassword);
    User deletedUser = User.builder().email(targetEmail).nickname("deletedUser").password(encodedPassword).build();
    deletedUser.delete();
    assertThat(deletedUser.isDeleted()).isTrue();

    when(userRepository.findByEmail(targetEmail)).thenReturn(Optional.of(deletedUser));
    // when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true); // 이 부분은 실행 전에 예외 발생 가능

    // when & then
    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(LoginFailedException.class);

    verify(userRepository).findByEmail(targetEmail);
  }


  // --- 닉네임 수정 테스트 ---
  @Test
  @DisplayName("성공: 존재하는 사용자의 닉네임 수정 성공")
  void updateNickname_whenUserExists_shouldUpdateNickname() {
    // given
    Long userId = 1L;
    String originalNickname = "tester";
    String newNickname = "updatedTester";
    String email = "test@example.com";
    String password = "encodedPassword";
    User existingUser = User.builder().email(email).nickname(originalNickname).password(password).build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    // when
    userService.updateNickname(userId, newNickname);

    // then
    verify(userRepository).findById(userId);
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getNickname()).isEqualTo(newNickname);
    assertThat(savedUser.getEmail()).isEqualTo(email);
  }

  @Test
  @DisplayName("실패: 존재하지 않는 사용자의 닉네임 수정 시 UserNotFoundException 발생")
  void updateNickname_whenUserNotFound_shouldThrowUserNotFoundException() {
    // given
    Long nonExistentUserId = 999L;
    String newNickname = "someNickname";
    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

    // when & then: 예외 타입 및 ErrorCode의 기본 메시지 검증
    assertThatThrownBy(() -> userService.updateNickname(nonExistentUserId, newNickname))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage(USER_NOT_FOUND.getMessage());

    verify(userRepository, never()).save(any(User.class));
  }


  // --- 논리 삭제 테스트 ---
  @Test
  @DisplayName("성공: 존재하는 사용자 논리 삭제 성공")
  void deleteUser_whenUserExists_marksUserAsDeleted() {
    // given
    Long userId = 1L;
    User existingUser = User.builder().email("delete@example.com").nickname("deleteUser").password("encodedPassword").build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository).findById(userId);
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.isDeleted()).isTrue();
  }

  @Test
  @DisplayName("실패: 존재하지 않는 사용자 논리 삭제 시 UserNotFoundException 발생")
  void deleteUser_whenUserNotFound_throwsUserNotFoundException() {
    // given
    Long nonExistentUserId = 999L;
    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

    // when & then: 예외 타입 및 ErrorCode의 기본 메시지 검증
    assertThatThrownBy(() -> userService.deleteUser(nonExistentUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage(USER_NOT_FOUND.getMessage()); // <<<--- 수정된 방식

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("성공: 존재하는 사용자 물리 삭제 성공") // DisplayName도 Green 상태에 맞게 수정 권장
  void hardDeleteUser_whenUserExists_shouldCallDeleteById() {
    // given: 삭제할 사용자 ID 및 해당 사용자가 존재한다고 Mocking
    Long userId = 1L;
    User existingUser = User.builder() // findById가 반환할 (삭제될) 사용자 객체
        .email("deleteMe@example.com")
        .nickname("deleteMe")
        .password("encodedPassword")
        .build();

    ReflectionTestUtils.setField(existingUser, "id", userId);

    // findByIdOrThrow 헬퍼 메소드를 사용할 것이므로 findById Mocking
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(existingUser));

    // when: 물리 삭제 서비스 메소드 호출
    userService.hardDeleteUser(userId);

    // then: findById와 deleteById 호출 검증
    verify(userRepository).findById(eq(userId)); // 사용자 조회 확인
    verify(userRepository).deleteById(eq(userId)); // deleteById가 올바른 ID(1L)로 호출되었는지 확인
  }
}
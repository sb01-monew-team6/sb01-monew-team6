package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.EmailAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.UserServiceImpl;
import com.sprint.part3.sb01_monew_team6.exception.UserNotFoundException;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock // 가짜(Mock) UserRepository 객체 생성
  private UserRepository userRepository;

  @Mock // 가짜(Mock) PasswordEncoder 객체 생성 (비밀번호 암호화용)
  private PasswordEncoder passwordEncoder;

  @InjectMocks // @Mock으로 생성된 객체들을 주입할 대상 클래스 지정
  private UserServiceImpl userService;

  // --- 회원가입 성공 테스트 ---
  @Test
  @DisplayName("성공: 신규 이메일로 회원가입 성공 시, 암호화된 비밀번호로 사용자 저장")
  void registerUser_whenEmailIsNew_savesUserWithEncodedPassword() {
    // given: 회원가입 요청 데이터 및 Mock 객체 설정
    UserRegisterRequest request = new UserRegisterRequest("new@example.com", "newUser", "password123");
    String encodedPassword = "encodedPassword"; // 암호화된 비밀번호 Mock 값

    // Mockito 설정
    when(userRepository.existsByEmail(request.email())).thenReturn(false); // 이메일 없음
    when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword); // 암호화 결과 설정

    // when: 회원가입 서비스 메소드 호출
    userService.registerUser(request);

    // then: Mock 객체 상호작용 및 전달된 데이터 검증
    // 1. existsByEmail 호출 검증
    verify(userRepository).existsByEmail(request.email());
    // 2. passwordEncoder.encode 호출 검증
    verify(passwordEncoder).encode(request.password());
    // 3. userRepository.save 호출 검증 및 전달된 User 객체 캡처
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture()); // save 호출 시 전달된 User 객체 잡기
    // 4. 캡처된 User 객체 검증
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo(request.email());
    assertThat(savedUser.getNickname()).isEqualTo(request.nickname());
    assertThat(savedUser.getPassword()).isEqualTo(encodedPassword); // 암호화된 비밀번호 확인
    assertThat(savedUser.isDeleted()).isFalse();
    assertThat(savedUser.getId()).isNull(); // save 메소드로 전달되는 시점엔 ID가 없음
  }

  // --- 이메일 중복 실패 테스트 ---
  @Test
  @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 EmailAlreadyExistsException 발생")
  void registerUser_whenEmailExists_throwsEmailAlreadyExistsException() {
    // given: 이미 존재하는 이메일을 가진 요청 데이터
    UserRegisterRequest request = new UserRegisterRequest("exists@example.com", "anotherUser", "password456");

    // Mockito 설정: userRepository.existsByEmail() 호출 시 true 반환하도록 설정 (이메일이 이미 존재함)
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    // when & then: registerUser 호출 시 EmailAlreadyExistsException 예외 발생하는지 검증
    assertThatThrownBy(() -> userService.registerUser(request)) // assertThatThrownBy 사용
        .isInstanceOf(EmailAlreadyExistsException.class) // 발생한 예외 타입 확인
        .hasMessageContaining("이미 가입된 이메일입니다: " + request.email()); // 예외 메시지 확인 (포함 여부)

    // then: 예외 발생 시 save는 호출되지 않았는지 검증 (회원가입 시도 중단 확인)
    verify(userRepository, never()).save(any(User.class)); // never(): 해당 Mock 상호작용이 전혀 없었는지 확인
    verify(passwordEncoder, never()).encode(anyString()); // 비밀번호 암호화도 호출되지 않아야 함
  }

  @Test
  @DisplayName("존재하는 사용자의 닉네임 수정 성공")
  void updateNickname_whenUserExists_shouldUpdateNickname() {
    // given
    Long userId = 1L;
    String originalNickname = "tester";
    String newNickname = "updatedTester";
    String email = "test@example.com";
    String password = "encodedPassword";

    User existingUser = User.builder()
        .email(email)
        .nickname(originalNickname)
        .password(password)
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser)); // ID로 찾으면 existingUser 반환

    // when
    userService.updateNickname(userId, newNickname);

    // then
    verify(userRepository).findById(userId); // findById 호출되었는지 확인

    // save 메소드에 전달된 User 객체를 캡처하여 검증
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture()); // save가 호출되었고, 전달된 User 객체 캡처
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getNickname()).isEqualTo(newNickname); // 캡처된 객체의 닉네임이 변경되었는지 확인
    assertThat(savedUser.getEmail()).isEqualTo(email); // 다른 필드는 그대로인지 확인
  }

  @Test
  @DisplayName("존재하지 않는 사용자의 닉네임 수정 시 UserNotFoundException 발생")
  void updateNickname_whenUserNotFound_shouldThrowUserNotFoundException() { // 메소드명 구체화
    // given
    Long nonExistentUserId = 999L;
    String newNickname = "someNickname";
    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty()); // 사용자 없음 Mocking

    // when & then
    assertThatThrownBy(() -> userService.updateNickname(nonExistentUserId, newNickname))
        .isInstanceOf(UserNotFoundException.class) // 예외 타입 확인
        .hasMessageContaining("사용자를 찾을 수 없습니다. ID: " + nonExistentUserId); // 예외 메시지 확인

    // then (추가 검증): 예외 발생 시 save는 호출되지 않아야 함
    verify(userRepository, never()).save(any(User.class));
  }


  @Test
  @DisplayName("존재하는 사용자 논리 삭제 성공")
  void deleteUser_whenUserExists_marksUserAsDeleted() {
    // given
    Long userId = 1L;
    User existingUser = User.builder()
        .email("delete@example.com")
        .nickname("deleteUser")
        .password("encodedPassword")
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository).findById(userId);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture()); // save 호출 및 객체 캡처
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.isDeleted()).isTrue(); // 논리 삭제 상태 확인
  }

  @Test
  @DisplayName("존재하지 않는 사용자 논리 삭제 시 UserNotFoundException 발생")
  void deleteUser_whenUserNotFound_throwsUserNotFoundException() { // 메소드명 구체화
    // given
    Long nonExistentUserId = 999L;
    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty()); // 사용자 없음 Mocking

    // when & then
    assertThatThrownBy(() -> userService.deleteUser(nonExistentUserId))
        .isInstanceOf(UserNotFoundException.class) // 예외 타입 확인
        .hasMessageContaining("사용자를 찾을 수 없습니다. ID: " + nonExistentUserId); // 예외 메시지 확인

    // then: 예외 발생 시 save는 호출되지 않았는지 검증
    verify(userRepository, never()).save(any(User.class));
  }
}
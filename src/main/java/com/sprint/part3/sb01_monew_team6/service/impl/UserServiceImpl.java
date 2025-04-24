package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.user.EmailAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.user.LoginFailedException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public User registerUser(UserRegisterRequest request) {
    // 1. 이메일 중복 확인 -> .getEmail() 대신 .email() 사용
    if (userRepository.existsByEmail(request.email())) {
      throw new EmailAlreadyExistsException(request.email());
    }
    // 2. 비밀번호 암호화 -> .getPassword() 대신 .password() 사용
    String encodedPassword = passwordEncoder.encode(request.password());

    // 3. User 엔티티 생성 -> .getEmail(), .getNickname() 대신 .email(), .nickname() 사용
    User newUser = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(encodedPassword)
        .build();

    // 4. User 저장
    return userRepository.save(newUser);
  }
  @Override
  @Transactional(readOnly = true) // 데이터 변경이 없으므로 읽기 전용 트랜잭션
  public User login(UserLoginRequest request) {
    // 1. 이메일로 사용자 조회 (findByEmail은 Optional<User> 반환)
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(LoginFailedException::new); // 사용자가 없으면 LoginFailedException 발생

    // 2. 비밀번호 확인 (입력된 비밀번호와 DB의 암호화된 비밀번호 비교)
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new LoginFailedException(); // 비밀번호 불일치 시 LoginFailedException 발생
    }

    // 3. 로그인 성공: 사용자 정보 반환 (삭제된 사용자는 로그인 불가 로직 추가 가능)
    if (user.isDeleted()) { // 예: 논리 삭제된 사용자인지 확인
      throw new LoginFailedException("삭제된 계정입니다."); // 또는 다른 예외
    }

    return user;
  }

  @Override
  @Transactional
  public void updateNickname(Long userId, String newNickname) {
    User user = findUserByIdOrThrow(userId); // 헬퍼 메소드 사용
    user.updateNickname(newNickname);
    userRepository.save(user); // 명시적 save 유지 시
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = findUserByIdOrThrow(userId); // 헬퍼 메소드 사용
    user.delete();
    userRepository.save(user); // 명시적 save 유지 시
  }

  private User findUserByIdOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
  }

}
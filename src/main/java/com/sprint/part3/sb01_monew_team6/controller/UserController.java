package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  /**
   * 사용자 등록(회원가입) 요청을 처리합니다.
   *
   * @param requestDto 사용자 등록 요청 정보를 담은 DTO (@Valid로 유효성 검사 수행)
   * @return 생성된 사용자 정보를 담은 UserDto와 HTTP 상태 코드 201 (Created)
   */
  @PostMapping // HTTP POST 요청을 /api/users 경로로 매핑합니다.
  @ResponseStatus(HttpStatus.CREATED) // 요청 성공 시 HTTP 상태 코드를 201 Created로 설정합니다.
  public UserDto registerUser(@Valid @RequestBody UserRegisterRequest requestDto) {
    User registeredUser = userService.registerUser(requestDto);
    return UserDto.fromEntity(registeredUser);
  }

  /**
   * 사용자 로그인 요청을 처리합니다.
   *
   * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
   * @return 로그인 성공 시 사용자 정보를 담은 UserDto와 HTTP 상태 코드 200 (OK)
   */
  @PostMapping("/login")
  public UserDto loginUser(@Valid @RequestBody UserLoginRequest loginRequest) {
    // 1. @Valid 와 @RequestBody 로 요청 검증 및 DTO 변환
    // 2. UserService의 login 메서드를 호출하여 로그인 로직 수행 (성공 시 User 엔티티 반환 가정)
    User loggedInUser = userService.login(loginRequest);

    // 3. 로그인 성공한 User 엔티티를 UserDto로 변환하여 반환 (상태 코드 200 OK는 기본값)
    return UserDto.fromEntity(loggedInUser);
  }




}
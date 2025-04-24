package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserNicknameUpdateRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    User loggedInUser = userService.login(loginRequest);
    return UserDto.fromEntity(loggedInUser);
  }
  /**
   * 사용자 닉네임 수정 요청을 처리합니다.
   * @param userId 수정할 사용자의 ID
   * @param request 새로운 닉네임 정보를 담은 DTO
   * @return 수정된 사용자 정보를 담은 UserDto와 HTTP 상태 코드 200 (OK)
   */
  @PatchMapping("/{userId}") // HTTP PATCH 요청을 /api/users/{userId} 경로로 매핑
  public ResponseEntity<UserDto> updateNickname(
      @PathVariable Long userId,
      @RequestBody @Valid UserNicknameUpdateRequest request
  ) {
    User updatedUser = userService.updateNickname(userId, request.nickname());
    UserDto responseDto = UserDto.fromEntity(updatedUser);
    return ResponseEntity.ok(responseDto);
  }
  // --- vvv 사용자 논리 삭제 엔드포인트 추가 vvv ---
  /**
   * 사용자 논리 삭제 요청을 처리합니다.
   * @param userId 삭제할 사용자의 ID
   * @return 성공 시 HTTP 상태 코드 204 (No Content)
   */
  @DeleteMapping("/{userId}") // HTTP DELETE 요청을 /api/users/{userId} 경로로 매핑
  public ResponseEntity<Void> deleteUser(
      @PathVariable Long userId // 경로 변수에서 userId 추출
  ) {
    // 1. 서비스 호출하여 사용자 논리 삭제 수행 (반환값 없음)
    userService.deleteUser(userId);

    // 2. ResponseEntity.noContent()를 사용하여 204 상태 코드 반환
    return ResponseEntity.noContent().build();
  }


}
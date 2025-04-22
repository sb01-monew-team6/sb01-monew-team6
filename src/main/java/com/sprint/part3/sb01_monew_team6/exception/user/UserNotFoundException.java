package com.sprint.part3.sb01_monew_team6.exception;

// 사용자를 찾을 수 없을 때 발생시킬 커스텀 예외
public class UserNotFoundException extends RuntimeException {

  // 사용자 ID를 포함하는 생성자
  public UserNotFoundException(Long userId) {
    super("사용자를 찾을 수 없습니다. ID: " + userId); // 예외 메시지
  }

  // 다른 메시지를 사용하는 생성자 (선택 사항)
  public UserNotFoundException(String message) {
    super(message);
  }
}
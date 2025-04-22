package com.sprint.part3.sb01_monew_team6.exception;

import org.springframework.security.core.AuthenticationException;

// 로그인 실패 시 사용할 커스텀 예외
public class LoginFailedException extends AuthenticationException {

  private static final String DEFAULT_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

  public LoginFailedException() {
    super(DEFAULT_MESSAGE);
  }

  public LoginFailedException(String message) {
    super(message);
  }
}
package com.sprint.part3.sb01_monew_team6.exception;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.LoginFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;

class LoginFailedExceptionTest {

  @Test
  @DisplayName("LoginFailedException(String) 생성자 테스트")
  void testConstructorWithStringArgument() {
    // given
    String customMessage = "This message is ignored by current constructor";

    // when: String을 인자로 받는 생성자 호출
    LoginFailedException exception = new LoginFailedException(customMessage);

    // then: 객체가 생성되고, 부모 생성자에 정의된 기본 속성들이 설정되었는지 확인
    assertThat(exception).isNotNull();
    assertThat(exception.getCode()).isEqualTo(ErrorCode.LOGIN_FAILED); // ErrorCode 확인
    assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED); // HttpStatus 확인
    assertThat(exception.getMessage()).isEqualTo(ErrorCode.LOGIN_FAILED.getMessage());
  }

  @Test
  @DisplayName("LoginFailedException() 기본 생성자 테스트")
  void testDefaultConstructor() {
    // when: 기본 생성자 호출
    LoginFailedException exception = new LoginFailedException();

    // then: 객체가 생성되고, 부모 생성자에 정의된 기본 속성들이 설정되었는지 확인
    assertThat(exception).isNotNull();
    assertThat(exception.getCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
    assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(exception.getMessage()).isEqualTo(ErrorCode.LOGIN_FAILED.getMessage());
  }
}
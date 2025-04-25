package com.sprint.part3.sb01_monew_team6.exception;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;

class UserNotFoundExceptionTest {

  @Test
  @DisplayName("UserNotFoundException(String) 생성자 테스트 - 커버리지 확보용")
  void testConstructorWithStringArgument() {
    // given
    String customMessage = "User not found with specific identifier";

    // when: String을 인자로 받는 생성자 호출
    UserNotFoundException exception = new UserNotFoundException(customMessage);

    // then: 객체가 생성되고, 부모 생성자에 정의된 기본 속성들이 설정되었는지 확인
    assertThat(exception).isNotNull();
    assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND); // ErrorCode 확인
    assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND); // HttpStatus 확인
    // 현재 구현상 생성자에 전달된 message는 사용되지 않고 ErrorCode의 메시지를 사용함
    assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("UserNotFoundException(Long) 생성자 테스트")
  void testConstructorWithLongArgument() {
    // given
    Long userId = 999L;

    // when: Long을 인자로 받는 생성자 호출
    UserNotFoundException exception = new UserNotFoundException(userId);

    // then: 객체가 생성되고, 부모 생성자에 정의된 기본 속성들이 설정되었는지 확인
    assertThat(exception).isNotNull();
    assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.getMessage());
  }
}
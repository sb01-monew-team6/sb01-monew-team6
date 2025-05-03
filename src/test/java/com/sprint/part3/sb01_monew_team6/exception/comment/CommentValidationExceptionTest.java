package com.sprint.part3.sb01_monew_team6.exception.comment;

import static org.assertj.core.api.Assertions.assertThat;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CommentValidationExceptionTest {

  @DisplayName("CommentValidationException 생성자 테스트")
  @Test
  void createCommentValidationException() {
    // given
    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    HttpStatus status = HttpStatus.BAD_REQUEST;

    // when
    CommentValidationException exception = new CommentValidationException();

    // then
    assertThat(exception.getCode()).isEqualTo(errorCode);
    assertThat(exception.getTimestamp()).isNotNull();
    assertThat(exception.getStatus()).isEqualTo(status);
    assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
  }
}

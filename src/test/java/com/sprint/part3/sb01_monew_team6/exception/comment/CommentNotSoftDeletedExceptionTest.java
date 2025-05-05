package com.sprint.part3.sb01_monew_team6.exception.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class CommentNotSoftDeletedExceptionTest {

  @DisplayName("CommentNotSoftDeletedException 생성자 테스트")
  @Test
  void createCommentNotSoftDeletedException(){
    // given
    ErrorCode errorCode = ErrorCode.COMMENT_NOT_SOFT_DELETED;
    HttpStatus status = HttpStatus.BAD_REQUEST;

    // when
    CommentNotSoftDeletedException exception = new CommentNotSoftDeletedException();

    // then
    assertThat(exception.getCode()).isEqualTo(errorCode);
    assertThat(exception.getTimestamp()).isNotNull();
    assertThat(exception.getStatus()).isEqualTo(status);
    assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
  }
}

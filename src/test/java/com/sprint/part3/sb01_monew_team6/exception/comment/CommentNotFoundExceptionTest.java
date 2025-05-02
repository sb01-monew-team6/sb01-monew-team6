package com.sprint.part3.sb01_monew_team6.exception.comment;

import static org.hamcrest.MatcherAssert.assertThat;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class CommentNotFoundExceptionTest {

  @DisplayName("CommentNotFoundException 생성자 테스트")
  @Test
  void createCommentNotFoundException(){
    //given
    Instant timeStamp = Instant.now();
    ErrorCode errorCode = ErrorCode.COMMENT_NOT_FOUND;
    HttpStatus status = HttpStatus.NOT_FOUND;

    // when
    CommentNotFoundException exception = new CommentNotFoundException();

    // then
    assertThat(exception.getCode()).isEqualTo(errorCode);
    assertThat(exception.getTimestamp()).isEqualTo(timeStamp);
    assertThat(exception.getStatus()).isEqualTo(status);
    assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
  }
}

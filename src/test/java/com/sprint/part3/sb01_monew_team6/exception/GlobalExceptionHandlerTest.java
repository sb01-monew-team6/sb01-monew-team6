package com.sprint.part3.sb01_monew_team6.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.sprint.part3.sb01_monew_team6.dto.ErrorResponse;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  @Test
  void shouldCreateErrorResponseFromMonewException() {
    // given
    CommentNotFoundException exception = new CommentNotFoundException();

    // when
    ErrorResponse response = handleMonewException(exception); // ⛔ 이 메서드 없음 → 컴파일 에러 발생해야 정상

    // then
    assertEquals("COMMENT_NOT_FOUND", response.code());
    assertEquals("댓글을 찾을 수 없습니다.", response.message());
    assertEquals("CommentNotFoundException", response.exceptionType());
    assertEquals(404, response.status());
    assertEquals(exception.getTimestamp(), response.timestamp());
  }

}
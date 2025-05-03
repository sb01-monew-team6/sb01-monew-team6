package com.sprint.part3.sb01_monew_team6.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.sprint.part3.sb01_monew_team6.dto.ErrorResponse;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  @Test
  void shouldReturnProperErrorResponse_whenMonewExceptionIsHandled() {
    // given
    Instant now = Instant.now();
    MonewException exception = new MonewException(ErrorCode.COMMENT_NOT_FOUND, now, HttpStatus.NOT_FOUND);
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleMonewException(exception);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    ErrorResponse body = responseEntity.getBody();
    assertNotNull(body);
    assertEquals(now, body.timestamp());
    assertEquals("COMMENT_NOT_FOUND", body.code());
    assertEquals("댓글을 찾을 수 없습니다.", body.message());
    assertEquals("MonewException", body.exceptionType());
    assertEquals(404, body.status());
  }

}
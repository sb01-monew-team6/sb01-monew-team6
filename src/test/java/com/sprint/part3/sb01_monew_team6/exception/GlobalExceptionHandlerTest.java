package com.sprint.part3.sb01_monew_team6.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sprint.part3.sb01_monew_team6.dto.ErrorResponse;
import com.sprint.part3.sb01_monew_team6.exception.comment.CommentNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @DisplayName("MethodArgumentNotValidException 처리 - ErrorResponse 생성 확인")
  @Test
  void handleValidationException_shouldReturnProperErrorResponse() {
    // given
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    ObjectError objectError = new ObjectError("content", "댓글 내용은 비어 있을 수 없습니다.");

    given(bindingResult.getAllErrors()).willReturn(List.of(objectError));
    given(exception.getBindingResult()).willReturn(bindingResult);

    // when
    ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("VALIDATION_ERROR");
    assertThat(body.message()).isEqualTo("댓글 내용은 비어 있을 수 없습니다.");
    assertThat(body.exceptionType()).isEqualTo("MethodArgumentNotValidException");
    assertThat(body.status()).isEqualTo(400);
    assertThat(body.timestamp()).isNotNull();
  }
}
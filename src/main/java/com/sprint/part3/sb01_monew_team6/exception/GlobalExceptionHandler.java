package com.sprint.part3.sb01_monew_team6.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sprint.part3.sb01_monew_team6.dto.ErrorResponse;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestException;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(exception = UserException.class)
	public ResponseEntity<ErrorResponse> handleUserException(UserException e) {

		ErrorCode code = e.getCode();
		return ResponseEntity
			.status(e.getStatus())
			.body(
				new ErrorResponse(
					e.getTimestamp(),
					code.toString(),
					code.getMessage(),
					UserException.class.getSimpleName(),
					e.getStatus().value()
				)
			);
	}

	@ExceptionHandler(exception = InterestException.class)
	public ResponseEntity<ErrorResponse> handleInterestException(InterestException e) {

		ErrorCode code = e.getCode();
		return ResponseEntity
			.status(e.getStatus())
			.body(
				new ErrorResponse(
					Instant.now(),
					code.toString(),
					code.getMessage(),
					InterestException.class.getSimpleName(),
					e.getStatus().value()
			)
		);
	}

	@ExceptionHandler(exception = NewsException.class)
	public ResponseEntity<ErrorResponse> handleNewsException(NewsException e) {

		return ResponseEntity.status(e.getStatus())
			.body(
				new ErrorResponse(
					e.getTimestamp(),
					e.getCode().toString(),
					e.getMessage(),
					NewsException.class.getSimpleName(),
					e.getStatus().value()
				)
			);
	}
}

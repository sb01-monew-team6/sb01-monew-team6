package com.sprint.part3.sb01_monew_team6.exception;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sprint.part3.sb01_monew_team6.dto.ErrorResponse;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestException;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.validation.group.NotificationValidationGroup;
import com.sprint.part3.sb01_monew_team6.validation.group.UserActivityValidationGroup;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Map<Class<?>, ErrorCode> VALIDATION_GROUP_ERROR_CODES = Map.of(
		NotificationValidationGroup.class, NOTIFICATION_INVALID_EXCEPTION,
		UserActivityValidationGroup.class, USER_ACTIVITY_INVALID_EXCEPTION
	);

	//
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

	//
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

	@ExceptionHandler(NotificationException.class)
	public ResponseEntity<ErrorResponse> handleNewsException(NotificationException e) {

		ErrorCode code = e.getCode();
		return ResponseEntity.status(e.getStatus())
			.body(
				new ErrorResponse(
					e.getTimestamp(),
					code.toString(),
					e.getMessage(),
					NewsException.class.getSimpleName(),
					e.getStatus().value()
				)
			);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleNotificationException(ConstraintViolationException e) {

		Optional<ErrorCode> matchedErrorCode = e.getConstraintViolations().stream()
			.flatMap(v -> v.getConstraintDescriptor().getGroups().stream())
			.filter(VALIDATION_GROUP_ERROR_CODES::containsKey)
			.findFirst()
			.map(VALIDATION_GROUP_ERROR_CODES::get);

		if (matchedErrorCode.isEmpty()) {
			throw e;
		}

		ErrorCode code = matchedErrorCode.get();
		HttpStatus status = BAD_REQUEST;
		return ResponseEntity
			.status(status)
			.body(
				new ErrorResponse(
					Instant.now(),
					code.toString(),
					code.getMessage(),
					e.getClass().getSimpleName(),
					status.value()
				)
			);
	}

	@ExceptionHandler(MonewException.class)
	public ResponseEntity<ErrorResponse> handleMonewException(MonewException e) {
		ErrorCode code = e.getCode();
		return ResponseEntity
				.status(e.getStatus())
				.body(new ErrorResponse(
						e.getTimestamp(),
						code.toString(),
						code.getMessage(),
						e.getClass().getSimpleName(),
						e.getStatus().value()
				));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		ErrorCode code = ErrorCode.VALIDATION_ERROR;

		String message = e.getBindingResult().getAllErrors()
				.stream()
				.findFirst()
				.map(ObjectError::getDefaultMessage)
				.orElse("입력값이 유효하지 않습니다.");

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST) // 직접 지정
				.body(new ErrorResponse(
						Instant.now(),
						code.name(),
						message,
						e.getClass().getSimpleName(),
						HttpStatus.BAD_REQUEST.value()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						Instant.now(),
						"INTERNAL_SERVER_ERROR",
						"예상치 못한 오류가 발생했습니다.",
						e.getClass().getSimpleName(),
						HttpStatus.INTERNAL_SERVER_ERROR.value()
				));
	}

}

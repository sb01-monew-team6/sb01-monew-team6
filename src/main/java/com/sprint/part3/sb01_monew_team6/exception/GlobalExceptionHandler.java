package com.sprint.part3.sb01_monew_team6.exception;

import com.sprint.part3.sb01_monew_team6.dto.ErrorResponse;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestException;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;
import com.sprint.part3.sb01_monew_team6.validation.group.NotificationValidationGroup;
import com.sprint.part3.sb01_monew_team6.validation.group.UserActivityValidationGroup;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Spring Security의 AccessDeniedException
import org.springframework.security.core.AuthenticationException; // Spring Security의 AuthenticationException
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// VALIDATION_GROUP_ERROR_CODES는 ErrorCode enum에 해당 상수가 정의되어 있어야 합니다.
	private static final Map<Class<?>, ErrorCode> VALIDATION_GROUP_ERROR_CODES = Map.of(
			NotificationValidationGroup.class, ErrorCode.NOTIFICATION_INVALID_EXCEPTION,
			UserActivityValidationGroup.class, ErrorCode.USER_ACTIVITY_INVALID_EXCEPTION
	);

	@ExceptionHandler(exception = UserException.class)
	public ResponseEntity<ErrorResponse> handleUserException(UserException e) {
		ErrorCode code = e.getCode();
		log.error("UserException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity
				.status(e.getStatus())
				.body(
						new ErrorResponse(
								e.getTimestamp(),
								code.name(),
								e.getMessage(),
								e.getClass().getSimpleName(), // 실제 발생한 예외 클래스명
								e.getStatus().value()
						)
				);
	}

	@ExceptionHandler(exception = InterestException.class)
	public ResponseEntity<ErrorResponse> handleInterestException(InterestException e) {
		ErrorCode code = e.getCode();
		log.error("InterestException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity
				.status(e.getStatus())
				.body(
						new ErrorResponse(
								e.getTimestamp(),
								code.name(),
								e.getMessage(),
								e.getClass().getSimpleName(),
								e.getStatus().value()
						)
				);
	}

	@ExceptionHandler(exception = SubscriptionException.class)
	public ResponseEntity<ErrorResponse> handleSubscriptionException(SubscriptionException e) {
		ErrorCode code = e.getCode();
		log.error("SubscriptionException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity
				.status(e.getStatus())
				.body(
						new ErrorResponse(
								e.getTimestamp(),
								code.name(),
								e.getMessage(),
								e.getClass().getSimpleName(),
								e.getStatus().value()
						)
				);
	}

	@ExceptionHandler(exception = NewsException.class)
	public ResponseEntity<ErrorResponse> handleNewsException(NewsException e) {
		ErrorCode code = e.getCode();
		log.error("NewsException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity.status(e.getStatus())
				.body(
						new ErrorResponse(
								e.getTimestamp(),
								code.name(),
								e.getMessage(),
								e.getClass().getSimpleName(),
								e.getStatus().value()
						)
				);
	}

	@ExceptionHandler(NotificationException.class)
	public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException e) {
		ErrorCode code = e.getCode();
		log.error("NotificationException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity.status(e.getStatus())
				.body(
						new ErrorResponse(
								e.getTimestamp(),
								code.name(),
								e.getMessage(),
								e.getClass().getSimpleName(),
								e.getStatus().value()
						)
				);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
		Optional<ErrorCode> matchedErrorCode = e.getConstraintViolations().stream()
				.flatMap(v -> v.getConstraintDescriptor().getGroups().stream())
				.filter(VALIDATION_GROUP_ERROR_CODES::containsKey)
				.findFirst()
				.map(VALIDATION_GROUP_ERROR_CODES::get);

		if (matchedErrorCode.isEmpty()) {
			log.error("Unhandled ConstraintViolationException: {}", e.getMessage(), e);
			ErrorCode code = ErrorCode.VALIDATION_ERROR;
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponse(
							Instant.now(),
							code.name(),
							"입력값 유효성 검증에 실패했습니다.",
							e.getClass().getSimpleName(),
							HttpStatus.BAD_REQUEST.value()
					));
		}

		ErrorCode code = matchedErrorCode.get();
		HttpStatus status = code.getStatus(); // ErrorCode에서 status 가져오기
		log.warn("ConstraintViolationException for group {}: {} - {}", code.name(), code.getMessage(), e.getMessage());
		return ResponseEntity
				.status(status)
				.body(
						new ErrorResponse(
								Instant.now(), // MonewException을 상속하지 않으므로 직접 생성
								code.name(),
								code.getMessage(),
								e.getClass().getSimpleName(),
								status.value()
						)
				);
	}

	@ExceptionHandler(MonewException.class)
	public ResponseEntity<ErrorResponse> handleMonewException(MonewException e) {
		ErrorCode code = e.getCode();
		log.error("MonewException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity
				.status(e.getStatus())
				.body(new ErrorResponse(
						e.getTimestamp(),
						code.name(),
						e.getMessage(),
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
				.orElse(code.getMessage());
		log.warn("MethodArgumentNotValidException: {} - {}", code.name(), message, e);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(
						Instant.now(),
						code.name(),
						message,
						e.getClass().getSimpleName(),
						HttpStatus.BAD_REQUEST.value()
				));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
		ErrorCode code = ErrorCode.VALIDATION_ERROR; // 또는 ErrorCode.INVALID_INPUT_VALUE
		String message = "필수 헤더 '" + e.getHeaderName() + "'가 누락되었습니다.";
		log.warn("MissingRequestHeaderException: {} - {}", code.name(), message, e);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(
						Instant.now(),
						code.name(),
						message,
						e.getClass().getSimpleName(),
						HttpStatus.BAD_REQUEST.value()
				));
	}

	@ExceptionHandler(AccessDeniedException.class) // org.springframework.security.access.AccessDeniedException
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
		ErrorCode code = ErrorCode.ACCESS_DENIED;
		log.warn("AccessDeniedException: {} - {}", code.name(), e.getMessage()); // 스택 트레이스 없이 로깅
		return ResponseEntity
				.status(code.getStatus())
				.body(new ErrorResponse(
						Instant.now(),
						code.name(),
						code.getMessage(),
						e.getClass().getSimpleName(),
						code.getStatus().value()
				));
	}

	@ExceptionHandler(AuthenticationException.class) // org.springframework.security.core.AuthenticationException
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
		ErrorCode code = ErrorCode.UNAUTHORIZED_ACCESS;
		log.warn("AuthenticationException: {} - {}", code.name(), e.getMessage()); // 스택 트레이스 없이 로깅
		return ResponseEntity
				.status(code.getStatus())
				.body(new ErrorResponse(
						Instant.now(),
						code.name(),
						code.getMessage(),
						e.getClass().getSimpleName(),
						code.getStatus().value()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
		ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
		log.error("UnexpectedException: {} - {}", code.name(), e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						Instant.now(),
						code.name(),
						code.getMessage(),
						e.getClass().getSimpleName(),
						HttpStatus.INTERNAL_SERVER_ERROR.value()
				));
	}
}

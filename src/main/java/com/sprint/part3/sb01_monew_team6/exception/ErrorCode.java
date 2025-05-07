package com.sprint.part3.sb01_monew_team6.exception;

import lombok.Getter;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.http.HttpStatus; // HttpStatus 임포트 누락된 경우 추가

@Getter
public enum ErrorCode {

	// User 관련
	EMAIL_ALREADY_EXISTS("이미 가입된 이메일입니다."),
	LOGIN_FAILED("이메일 또는 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
	USER_LOGIN_FAILED_EXCEPTION("이메일 또는 비밀번호가 올바르지 않습니다."),

	// Interest 관련
	INTEREST_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	INTEREST_ALREADY_EXISTS("이미 존재하는 관심사 이름입니다."),
	INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심사를 찾을 수 없습니다."),
	INTEREST_NAME_TOO_SIMILAR("기존 관심사와 이름이 너무 유사합니다."),

	SUBSCRIPTION_ALREADY_EXISTS("이미 구독 중인 관심사입니다."),
	SUBSCRIPTION_NOT_FOUND("구독 정보를 찾을 수 없습니다."),

	//news
	NEWS_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	NEWS_NAVERCLIENT_EXCEPTION("NAVER API 요청 오류입니다."),
	NEWS_RSSCLIENT_EXCEPTION("RSS API 요청 오류입니다."),
	NEWS_NO_INTEREST_EXCEPTION("저장할 관심사가 없습니다."),
	NEWS_NO_NEW_NEWS_EXCEPTION("저장할 새로운 뉴스가 없습니다."),
	NEWS_BATCH_NO_INTEREST_EXCEPTION("Batch - 수집할 관심사가 없습니다."),
	NEWS_BATCH_NO_NEWS_EXCEPTION("Batch - 저장 대상 뉴스가 없습니다."),
	NEWS_ARTICLE_NOT_FOUND_EXCEPTION("기사가 존재하지 않습니다."),
	NEWS_NOT_USER_FOUND_EXCEPTION("유저가 존재하지 않습니다."),
	NEWS_LIMIT_MORE_THAN_ONE_EXCEPTION("limit은 1 이상의 값이어야 합니다."),
	NEWS_ORDERBY_IS_NOT_SUPPORT_EXCEPTION("지원하지 않는 orderBy 입니다."),
	NEWS_CALL_NEWSARTICLEREPOSITORY_EXCEPTION("NewsArticleRepository 호출 중 에러"),
	NESW_BACKUP_SERIALIZATION_FAILED_EXCEPTION("JSON 직렬화 오류"),
	NEWS_BACKUP_S3_UPLOAD_FAILED_EXCEPTION("S3 업로드 실패"),
	//notification
	NOTIFICATION_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	NOTIFICATION_USER_NOT_FOUND_EXCEPTION("존재하지 않는 유저입니다."),

	//user_activity
	USER_ACTIVITY_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	USER_ACTIVITY_NOT_FOUND_EXCEPTION("존재하지 않는 유저입니다."),

	// comment
	COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다."),
	ALREADY_LIKED_COMMENT("이미 좋아요한 댓글입니다."),
	COMMENT_LIKE_NOT_FOUND("댓글 좋아요를 찾을 수 없습니다."),

	VALIDATION_ERROR("입력값이 유효하지 않습니다."),
	COMMENT_NOT_SOFT_DELETED("논리 삭제되지 않은 댓글은 물리 삭제할 수 없습니다."),
	//article_view
	ARTICLE_VIEW_NOT_FOUND_EXCEPTION("기사 기록을 찾을 수 없습니다."),

	//보안 관련
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."), // 403 Forbidden
	UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."), // 401 Unauthorized

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다."); // <<<--- 메시지 수정 (테스트 기대값 일치)


	private final HttpStatus status; // HttpStatus 필드 추가 (기존 코드에 없었다면)
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	ErrorCode(String message) {
		this(HttpStatus.BAD_REQUEST, message); // 예시: 기본 상태 BAD_REQUEST
	}

	public HttpStatus getStatus() {
		return status;
	}
	public String getMessage() {
		return message;
	}
}

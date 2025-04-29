package com.sprint.part3.sb01_monew_team6.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

	// User 관련
	EMAIL_ALREADY_EXISTS("이미 가입된 이메일입니다."),
	LOGIN_FAILED("이메일 또는 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
	USER_LOGIN_FAILED_EXCEPTION("이메일 또는 비밀번호가 올바르지 않습니다."),

	//interest
	INTEREST_INVALID_EXCEPTION("유효하지 않은 요청입니다."), // TODO: 나중에 프로토타입의 예외 메시지로 수정할 것

	//news
	NEWS_INVALID_EXCEPTION("유효하지 않은 요청입니다."), // TODO: 나중에 프로토타입의 예외 메시지로 수정할 것
	NEWS_NAVERCLIENT_EXCEPTION("NAVER API 요청 오류입니다."),
	NEWS_RSSCLIENT_EXCEPTION("RSS API 요청 오류입니다."),
	NEWS_NO_INTEREST_EXCEPTION("저장할 관심사가 없습니다."),
	NEWS_NO_NEW_NEWS_EXCEPTION("저장할 새로운 뉴스가 없습니다."),
	NEWS_BATCH_NO_INTEREST_EXCEPTION("Batch - 수집할 관심사가 없습니다."),
	NEWS_BATCH_NO_NEWS_EXCEPTION("Batch - 저장 대상 뉴스가 없습니다."),
	NEWS_ARTICLE_NOT_FOUND_EXCEPTION("기사가 존재하지 않습니다."),
	NEWS_NOT_USER_FOUND_EXCEPTION("유저가 존재하지 않습니다."),
	//notification
	NOTIFICATION_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	NOTIFICATION_USER_NOT_FOUND_EXCEPTION("존재하지 않는 유저입니다."),

	//user_activity
	USER_ACTIVITY_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	USER_ACTIVITY_NOT_FOUND_EXCEPTION("존재하지 않는 유저입니다."),

	// 기타
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

	ErrorCode(String message) {
		this.message = message;
	}

	private final String message;

}
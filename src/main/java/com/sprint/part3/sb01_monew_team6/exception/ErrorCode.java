package com.sprint.part3.sb01_monew_team6.exception; // exception 패키지 확인

import lombok.Getter;

@Getter // 각 enum 상수가 message 필드에 대한 getter를 갖도록 함
public enum ErrorCode {

	// User 관련
	EMAIL_ALREADY_EXISTS("이미 가입된 이메일입니다."),
	LOGIN_FAILED("이메일 또는 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
	USER_LOGIN_FAILED_EXCEPTION("이메일 또는 비밀번호가 올바르지 않습니다."),

	// Interest 관련
	INTEREST_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	// INTEREST_NOT_FOUND("관심사를 찾을 수 없습니다."), // 필요하다면 나중에 추가

	// News 관련
	NEWS_INVALID_EXCEPTION("유효하지 않은 요청입니다."),
	// NEWS_NOT_FOUND("뉴스를 찾을 수 없습니다."), // 필요하다면 나중에 추가

	// 기타
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

	private final String message; // 각 에러 코드에 해당하는 메시지

	// 생성자: 각 enum 상수가 생성될 때 메시지를 받아서 초기화
	ErrorCode(String message) {
		this.message = message;
	}
}
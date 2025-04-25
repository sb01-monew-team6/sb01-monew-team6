package com.sprint.part3.sb01_monew_team6.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	//user
	USER_LOGIN_FAILED_EXCEPTION("이메일 또는 비밀번호가 올바르지 않습니다."),

	//interest
	INTEREST_INVALID_EXCEPTION("유효하지 않은 요청입니다."), // TODO: 나중에 프로토타입의 예외 메시지로 수정할 것

	//news
	NEWS_INVALID_EXCEPTION("유효하지 않은 요청입니다."), // TODO: 나중에 프로토타입의 예외 메시지로 수정할 것
	NEWS_NAVERCLIENT_EXCEPTION("NAVER API 요청 오류입니다."),
	NEWS_RSSCLIENT_EXCEPTION("RSS API 요청 오류입니다.")

	;

	ErrorCode(String message) {
		this.message = message;
	}

	private final String message;

}

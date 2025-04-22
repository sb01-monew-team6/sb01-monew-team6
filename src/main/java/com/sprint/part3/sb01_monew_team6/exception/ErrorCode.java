package com.sprint.part3.sb01_monew_team6.exception; // exception 패키지 확인

import lombok.Getter;

@Getter // 각 enum 상수가 message 필드에 대한 getter를 갖도록 함
public enum ErrorCode {

	// User 관련 (TDD에서 사용된 예외들에 맞춰 추가/수정)
	EMAIL_ALREADY_EXISTS("이미 가입된 이메일입니다."),     // 회원가입 시 이메일 중복
	LOGIN_FAILED("이메일 또는 비밀번호가 올바르지 않습니다."), // 로그인 실패 (ID 또는 PW 불일치)
	USER_NOT_FOUND("사용자를 찾을 수 없습니다."),         // 사용자 정보 조회/수정/삭제 실패

	// Interest 관련 (기존 코드 유지 또는 필요시 구체화)
	INTEREST_NOT_FOUND("관심사를 찾을 수 없습니다."), // <--- 구체적인 코드 예시

	// News 관련 (기존 코드 유지 또는 필요시 구체화)
	NEWS_NOT_FOUND("뉴스를 찾을 수 없습니다."), // <--- 구체적인 코드 예시

	// 기타 오류 (예시)
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."); // 예측하지 못한 오류

	private final String message; // 각 에러 코드에 해당하는 메시지

	// 생성자: 각 enum 상수가 생성될 때 메시지를 받아서 초기화
	ErrorCode(String message) {
		this.message = message;
	}
}
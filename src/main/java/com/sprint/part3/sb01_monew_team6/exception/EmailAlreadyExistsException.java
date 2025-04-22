package com.sprint.part3.sb01_monew_team6.exception; // exception 패키지 사용 예시

// 이메일 중복 시 발생시킬 커스텀 예외 클래스
public class EmailAlreadyExistsException extends RuntimeException {

  // 어떤 이메일이 중복되었는지 메시지에 포함시키는 생성자
  public EmailAlreadyExistsException(String email) {
    super("이미 가입된 이메일입니다: " + email); // 예외 메시지 설정
  }
}
package com.sprint.part3.sb01_monew_team6.dto;

public record UserRegisterRequest(
    String email,
    String nickname,
    String password // 비밀번호 필드 (유효성 검증은 별도 필요)
) {
}
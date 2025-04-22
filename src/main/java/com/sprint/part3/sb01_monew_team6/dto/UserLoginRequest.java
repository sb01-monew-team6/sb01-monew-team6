package com.sprint.part3.sb01_monew_team6.dto;

public record UserLoginRequest(
    String email,
    String password // 암호화되지 않은 원본 비밀번호
) {
}
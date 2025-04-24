package com.sprint.part3.sb01_monew_team6.dto;

import jakarta.validation.constraints.NotBlank; // Validation 어노테이션
import jakarta.validation.constraints.Size;

// 닉네임 수정 요청 시 사용할 DTO (record 사용)
public record UserNicknameUpdateRequest(
    @NotBlank(message = "닉네임은 비워둘 수 없습니다.") // 비어있지 않음 검증
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.") // 길이 검증 예시
    String nickname
) {
}
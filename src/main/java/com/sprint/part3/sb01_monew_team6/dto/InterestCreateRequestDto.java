package com.sprint.part3.sb01_monew_team6.dto;

import jakarta.validation.constraints.NotBlank; // @NotBlank 임포트
import jakarta.validation.constraints.NotEmpty; // @NotEmpty 임포트 (선택 사항)
import jakarta.validation.constraints.Size;    // @Size 임포트

import java.util.List;

/**
 * 관심사 등록 요청 DTO
 */
public record InterestCreateRequestDto(
    @NotBlank(message = "관심사 이름은 필수입니다.") // 이름은 비어있을 수 없음
    @Size(min = 1, max = 255, message = "관심사 이름은 1자 이상 255자 이하로 입력해주세요.") // 이름 길이 제한
    String name,
    List<String> keywords // 키워드 목록
) {
}

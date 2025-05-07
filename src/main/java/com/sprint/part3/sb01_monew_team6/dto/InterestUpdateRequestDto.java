package com.sprint.part3.sb01_monew_team6.dto;

import jakarta.validation.constraints.Size; // @Size 임포트
import java.util.List;

/**
 * 관심사 정보 수정 요청 DTO
 * 수정할 필드만 포함하며, 각 필드는 null일 수 있습니다 (null이면 해당 필드는 수정하지 않음을 의미).
 */
public record InterestUpdateRequestDto(
    @Size(min = 1, max = 255, message = "관심사 이름은 1자 이상 255자 이하로 입력해주세요.")
    String name, // 변경할 이름 (null 가능)

    List<String> keywords // 변경할 키워드 목록 (null 가능)
) {
}

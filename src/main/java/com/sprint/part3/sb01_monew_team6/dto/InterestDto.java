package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import lombok.Builder;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Builder
public record InterestDto(
    Long id,
    String name,
    List<String> keywords, // 키워드는 다시 List 형태로 변환하여 제공
    Long subscriberCount,
    Instant createdAt,
    Instant updatedAt
) {
  private static final String KEYWORD_DELIMITER = ","; // 서비스 로직과 동일한 구분자

  // Interest 엔티티를 InterestDto로 변환하는 정적 팩토리 메서드
  public static InterestDto fromEntity(Interest interest) {
    if (interest == null) {
      return null;
    }

    // 저장된 문자열 키워드를 List<String>으로 변환
    List<String> keywordList = (interest.getKeywords() != null && !interest.getKeywords().isEmpty())
        ? Arrays.asList(interest.getKeywords().split(KEYWORD_DELIMITER))
        : Collections.emptyList();

    return InterestDto.builder()
        .id(interest.getId())
        .name(interest.getName())
        .keywords(keywordList) // 변환된 리스트 사용
        .subscriberCount(interest.getSubscriberCount())
        .createdAt(interest.getCreatedAt())
        .updatedAt(interest.getUpdatedAt())
        .build();
  }
}

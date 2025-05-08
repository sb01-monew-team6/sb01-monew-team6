package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.Subscription;
import lombok.Builder;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Builder
public record SubscriptionDto(
    Long id, // 구독 자체의 ID
    Long interestId, // 구독한 관심사의 ID
    String interestName, // 구독한 관심사의 이름
    List<String> interestKeywords, // 구독한 관심사의 키워드 목록
    Long interestSubscriberCount, // 구독한 관심사의 구독자 수
    Instant createdAt // 구독 생성 시각
) {
  private static final String KEYWORD_DELIMITER = ",";

  public static SubscriptionDto fromEntity(Subscription subscription) {
    if (subscription == null) {
      return null;
    }
    Interest interest = subscription.getInterest();
    if (interest == null) {
      return null;
    }

    List<String> keywordList = (interest.getKeywords() != null && !interest.getKeywords().isEmpty())
        ? Arrays.asList(interest.getKeywords().split(KEYWORD_DELIMITER))
        : Collections.emptyList();

    return SubscriptionDto.builder()
        .id(subscription.getId())
        .interestId(interest.getId())
        .interestName(interest.getName())
        .interestKeywords(keywordList)
        .interestSubscriberCount(interest.getSubscriberCount())
        .createdAt(subscription.getCreatedAt())
        .build();
  }
}

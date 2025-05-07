package com.sprint.part3.sb01_monew_team6.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import lombok.Builder;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonPropertyOrder({ "id", "name", "keywords", "subscriberCount", "createdAt", "updatedAt", "subscribed" })
@Builder
public record InterestDto(
    Long id,
    String name,
    List<String> keywords,
    Long subscriberCount,
    @JsonProperty("subscribedByMe")
    boolean subscribed,
    Instant createdAt,
    Instant updatedAt

) {
  private static final String KEYWORD_DELIMITER = ",";

  public static InterestDto fromEntity(Interest interest, boolean subscribed) {
    if (interest == null) {
      return null;
    }

    List<String> keywordList = (interest.getKeywords() != null && !interest.getKeywords().isEmpty())
        ? Arrays.asList(interest.getKeywords().split(KEYWORD_DELIMITER))
        : Collections.emptyList();

    return InterestDto.builder()
        .id(interest.getId())
        .name(interest.getName())
        .keywords(keywordList)
        .subscriberCount(interest.getSubscriberCount())
        .createdAt(interest.getCreatedAt())
        .updatedAt(interest.getUpdatedAt())
        .subscribed(subscribed)
        .build();
  }
}

package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Builder
@AllArgsConstructor
public class Interest extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "keyword", columnDefinition = "TEXT")
  private String keywords;

  @Builder.Default
  @Column(nullable = false)
  private Long subscriberCount = 0L;

  /*
  키워드 문자열을 업데이트합니다.
  updatedAt은 BaseUpdatableEntity의 @LastModifiedDate에 의해 자동으로 처리됩니다.
  @param newKeywordsString 새로운 키워드 문자열 (예: "키워드1,키워드2")
  */
  public void updateKeywords(String newKeywordsString) {
    this.keywords = newKeywordsString;// setUpdatedAt(Instant.now()); // BaseUpdatableEntity에서 @LastModifiedDate로 자동 처리되므로 제거
  }

  /*
  이름을 업데이트합니다.
  updatedAt은 BaseUpdatableEntity의 @LastModifiedDate에 의해 자동으로 처리됩니다.
  @param newName 새로운 이름
  */
  public void setName(String newName) {
    this.name = newName; // 이름 필드 업데이트// setUpdatedAt(Instant.now()); // BaseUpdatableEntity에서 @LastModifiedDate로 자동 처리되므로 제거
  }

      // 구독자 수 증가/감소 메서드는 그대로 유지
  public void incrementSubscriberCount() {
    this.subscriberCount++;
  }

  public void decrementSubscriberCount() {
    if (this.subscriberCount > 0) {
      this.subscriberCount--;
    }
  }

  public List<String> getKeywordList() {
    if (keywords == null || keywords.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(keywords.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

}
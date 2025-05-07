package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity; // BaseUpdatableEntity 임포트
import jakarta.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
// import java.time.Instant; // BaseUpdatableEntity 에서 상속받으므로 제거

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

  /**
   * 키워드 문자열을 업데이트합니다.
   * updatedAt은 BaseUpdatableEntity의 @LastModifiedDate에 의해 자동으로 처리됩니다.
   * @param newKeywordsString 새로운 키워드 문자열 (예: "키워드1,키워드2")
   */
  public void updateKeywords(String newKeywordsString) {
    this.keywords = newKeywordsString;
  }

  /**
   * 이름을 업데이트합니다.
   * updatedAt은 BaseUpdatableEntity의 @LastModifiedDate에 의해 자동으로 처리됩니다.
   * @param newName 새로운 이름
   */
  public void setName(String newName) {
    this.name = newName;
  }

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

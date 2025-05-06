package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Builder // <<<--- 클래스 레벨로 이동
@AllArgsConstructor // <<<--- 모든 필드를 받는 생성자 자동 생성 (Builder가 사용)
public class Interest extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "keyword", columnDefinition = "TEXT")
  private String keywords;

  @Builder.Default // Lombok 빌더에 subscriberCount 필드 포함 및 기본값 설정
  @Column(nullable = false)
  private Long subscriberCount = 0L;

  // --- 생성자에 붙어있던 @Builder 제거됨 ---
  // @Builder // 제거됨
  // public Interest(String name, String keywords, Long subscriberCount) { ... } // @AllArgsConstructor가 대체

  /**
   * 키워드 문자열을 업데이트합니다.
   * @param newKeywordsString 새로운 키워드 문자열 (예: "키워드1,키워드2")
   */
  public void updateKeywords(String newKeywordsString) {
    this.keywords = newKeywordsString;
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
}

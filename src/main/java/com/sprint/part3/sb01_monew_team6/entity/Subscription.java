package com.sprint.part3.sb01_monew_team6.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

/**
 * 사용자-관심사 구독 관계를 나타내는 엔티티
 */
@Entity
@Table(name = "subscriptions",
    uniqueConstraints = {
        // 한 사용자는 같은 관심사를 중복 구독할 수 없음
        @UniqueConstraint(columnNames = {"user_id", "interest_id"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "interest"}) // 순환 참조 방지
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false) // 사용자는 필수
  @JoinColumn(name = "user_id", nullable = false) // 외래 키 설정
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false) // 관심사도 필수
  @JoinColumn(name = "interest_id", nullable = false) // 외래 키 설정
  private Interest interest;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now(); // 구독 시작 시각

  // 빌더 패턴 (생성 시 user와 interest를 필수로 받도록)
  @Builder
  public Subscription(User user, Interest interest) {
    if (user == null || interest == null) {
      throw new IllegalArgumentException("User and Interest must not be null for Subscription");
    }
    this.user = user;
    this.interest = interest;
    // createdAt은 기본값 사용
  }

  // 연관관계 편의 메서드 등은 필요 시 추가 (예: User나 Interest 엔티티에서 구독 목록 관리 시)

}

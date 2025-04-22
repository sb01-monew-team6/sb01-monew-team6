package com.sprint.part3.sb01_monew_team6.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
// import org.hibernate.annotations.ColumnDefault; // 필요 시 DB 레벨 기본값 설정용
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant; // 타임스탬프 타입

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users", indexes = { // 데이터베이스 테이블 이름 명시 및 인덱스 설정
    @Index(name = "idx_users_email", columnList = "email", unique = true)

})
public class User {

  @Id // 기본 키(PK) 필드 지정
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 255) // 컬럼 제약조건: Not Null, 길이 255
  private String nickname;

  @Column(nullable = false)
  private String password; // 해시된 비밀번호

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt; // 생성 시각 (TIMESTAMPTZ와 매핑)

  @LastModifiedDate
  @Column
  private Instant updatedAt;

  @Column(nullable = false)
  // @ColumnDefault("false")
  private boolean deleted = false; // 논리 삭제 플래그 (Java 레벨 기본값 설정)

  @Builder
  public User(String email, String nickname, String password) {
    // 필수 값 검증
    if (email == null || email.isBlank() || nickname == null || nickname.isBlank() || password == null || password.isBlank()) {
      throw new IllegalArgumentException("Email, nickname, password는 비어 있을 수 없습니다.");
    }
    this.email = email;
    this.nickname = nickname;
    this.password = password;
    this.deleted = false; // 생성 시 기본값
  }

  // 닉네임 수정 메소드
  public void updateNickname(String newNickname) {
    if (newNickname == null || newNickname.isBlank()) {
      throw new IllegalArgumentException("새 닉네임은 비어 있을 수 없습니다.");
    }
    this.nickname = newNickname;
  }

  // 논리 삭제 메소드 (이름: delete)
  public void delete() {
    this.deleted = true;
    // updatedAt은 @LastModifiedDate에 의해 자동으로 갱신됨
  }
}
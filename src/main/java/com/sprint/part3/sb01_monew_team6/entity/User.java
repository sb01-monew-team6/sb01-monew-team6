package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})

public class User extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 255)
  private String nickname;

  @Column(nullable = false)
  private String password;

  @Column(name = "is_deleted", nullable = false) // 컬럼명 명시적 지정 (DDL과 일치)
  private boolean deleted = false;

  @Builder
  public User(String email, String nickname, String password) {

    if (email == null || email.isBlank() || nickname == null || nickname.isBlank() || password == null || password.isBlank()) {
      throw new IllegalArgumentException("Email, nickname, password는 비어 있을 수 없습니다.");
    }
    this.email = email;
    this.nickname = nickname;
    this.password = password;
    this.deleted = false;
  }


  public void updateNickname(String newNickname) {
    if (newNickname == null || newNickname.isBlank()) {
      throw new IllegalArgumentException("새 닉네임은 비어 있을 수 없습니다.");
    }
    this.nickname = newNickname;

  }

  public void delete() {
    this.deleted = true;

  }


}
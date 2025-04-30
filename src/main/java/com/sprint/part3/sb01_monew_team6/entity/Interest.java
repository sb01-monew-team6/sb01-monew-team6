package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "interests", indexes = {
    @Index(name = "uq_interests_name", columnList = "name", unique = true)
})
public class Interest extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true)
  private String name;

  @Type(ListArrayType.class)
  @Column(name = "keyword", columnDefinition = "text[]")
  private List<String> keywords = new ArrayList<>();

  @Column(nullable = false)
  private long subscriberCount = 0L;

  @Builder
  public Interest(String name, List<String> keywords) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("관심사 이름은 비워둘 수 없습니다.");
    }
    this.name = name;
    if (keywords != null) {
      this.keywords = new ArrayList<>(keywords);
    }
  }

  public void updateKeywords(List<String> newKeywords) {
    this.keywords = (newKeywords == null) ? new ArrayList<>() : new ArrayList<>(newKeywords);
  }
}

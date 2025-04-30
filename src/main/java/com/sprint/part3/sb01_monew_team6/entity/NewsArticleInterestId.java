package com.sprint.part3.sb01_monew_team6.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable // 다른 엔티티에 포함될 수 있음
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleInterestId implements Serializable { // 복합 기본 키 클래스, 객체를 직렬화
  private Long newsArticleId; //기사id
  private Long interestId; //관심사id
}
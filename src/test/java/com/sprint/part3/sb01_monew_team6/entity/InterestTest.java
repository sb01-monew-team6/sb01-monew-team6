package com.sprint.part3.sb01_monew_team6.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// import java.util.List; // List 사용 안 함

import static org.assertj.core.api.Assertions.*;

class InterestTest {

  // 키워드 구분자 (서비스 로직과 일치시켜야 함)
  private static final String KEYWORD_DELIMITER = ",";

  @Test
  @DisplayName("Interest 객체를 정상적으로 생성하고 기본 상태 검증 (keywords: String)")
  void createInterestSuccessfully() {
    // given: 생성에 필요한 데이터
    String name = "스포츠";
    // --- List<String> 대신 구분자로 연결된 String 사용 ---
    String keywordsString = "야구" + KEYWORD_DELIMITER + "축구" + KEYWORD_DELIMITER + "농구";

    // when: Interest 객체 생성 (Builder 사용)
    Interest interest = Interest.builder()
        .name(name)
        .keywords(keywordsString) // 문자열로 전달
        .build();

    // then: 초기 상태 검증
    assertThat(interest).isNotNull();
    assertThat(interest.getId()).isNull();
    assertThat(interest.getName()).isEqualTo(name);
    // --- AssertJ의 문자열 비교 메서드 사용 ---
    assertThat(interest.getKeywords()).isEqualTo(keywordsString);
    assertThat(interest.getSubscriberCount()).isZero();
  }

  @Test
  @DisplayName("updateKeywords 메소드로 키워드 문자열을 변경한다")
  void updateKeywords_shouldChangeKeywordsString() {
    // given
    String initialKeywords = "기존키워드1" + KEYWORD_DELIMITER + "기존키워드2";
    Interest interest = Interest.builder()
        .name("초기 관심사")
        .keywords(initialKeywords) // 문자열로 생성
        .build();
    String newKeywordsString = "새키워드1" + KEYWORD_DELIMITER + "새키워드2" + KEYWORD_DELIMITER + "새키워드3";

    // when
    interest.updateKeywords(newKeywordsString); // 문자열 파라미터로 호출

    // then
    // --- AssertJ의 문자열 비교 메서드 사용 ---
    assertThat(interest.getKeywords()).isEqualTo(newKeywordsString);
  }
}

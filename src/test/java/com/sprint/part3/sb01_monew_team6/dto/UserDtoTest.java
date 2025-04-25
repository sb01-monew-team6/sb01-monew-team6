package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class UserDtoTest {

  @Test
  @DisplayName("성공: fromEntity - User 객체가 null일 때 null을 반환한다")
  void fromEntity_whenUserIsNull_returnsNull() {
    // given: User 객체가 null인 상황
    User nullUser = null;

    // when: fromEntity 메소드 호출
    UserDto result = UserDto.fromEntity(nullUser);

    // then: 결과가 null인지 확인
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("성공: fromEntity - 정상 User 객체로 UserDto를 생성한다")
  void fromEntity_whenUserIsNotNull_returnsCorrectDto() {
    // given: 정상적인 User 객체 준비
    Instant now = Instant.now();
    User user = User.builder()
        .email("dto@example.com")
        .nickname("dtoUser")
        .password("pwd")
        .build();
    // User 엔티티에 ID와 createdAt이 설정되어 있다고 가정
    ReflectionTestUtils.setField(user, "id", 10L);
    ReflectionTestUtils.setField(user, "createdAt", now);

    // when: fromEntity 메소드 호출
    UserDto result = UserDto.fromEntity(user);

    // then: 반환된 DTO의 필드 값들이 User 객체와 일치하는지 확인
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(10L);
    assertThat(result.email()).isEqualTo("dto@example.com");
    assertThat(result.nickname()).isEqualTo("dtoUser");
    assertThat(result.createdAt()).isEqualTo(now);
  }
}
package com.sprint.part3.sb01_monew_team6.entity; // 수정된 패키지 경로

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserTest {

  @Test
  @DisplayName("User 객체를 정상적으로 생성한다.")
  void createUserSuccessfully() {
    // given: 생성에 필요한 데이터 준비
    String email = "test@example.com";
    String nickname = "tester";
    String hashedPassword = "hashedPassword123"; // 실제로는 외부에서 해싱된 값

    // when: User 객체 생성 시도
  User user = new User(email, nickname, hashedPassword);

    // then: 생성된 객체의 상태 검증 (아직 User 클래스 및 Getter가 없으므로 컴파일 에러 발생)
     assertThat(user).isNotNull();
     assertThat(user.getEmail()).isEqualTo(email);
     assertThat(user.getNickname()).isEqualTo(nickname);
     assertThat(user.getPassword()).isEqualTo(hashedPassword);
     assertThat(user.isDeleted()).isFalse();

  }
  @Test
  @DisplayName("updateNickname 메소드로 닉네임을 변경한다.")
  void updateNickname_ChangesNickname() {
    // given
    User user = new User("test@example.com", "tester", "hashedPassword123");
    String newNickname = "updatedTester";
    String originalEmail = user.getEmail();

    // when
    user.updateNickname(newNickname);

    // then
    assertThat(user.getNickname()).isEqualTo(newNickname);
    assertThat(user.getEmail()).isEqualTo(originalEmail); // 이메일은 변경되지 않음
  }
  @Test
  @DisplayName("delete 메소드로 isDeleted 상태를 true로 변경한다.")
  void delete_SetsIsDeletedToTrue() {
    // given
    User user = new User("test@example.com", "tester", "hashedPassword123");
    assertThat(user.isDeleted()).isFalse(); // 초기 상태 확인

    // when: delete 메소드 호출 시도
     user.delete();

    // then: isDeleted 상태 변경 확인
     assertThat(user.isDeleted()).isTrue();
  }









}
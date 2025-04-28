package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import com.sprint.part3.sb01_monew_team6.config.JpaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("성공: 사용자를 저장하고 ID로 조회하면 해당 사용자가 반환된다.")
  void saveAndFindById_returnsSavedUser() {
    // given: 저장할 User 객체 생성
    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();

    // when: User 저장 및 ID로 조회
    User savedUser = userRepository.save(user);
    assertThat(savedUser.getId()).isNotNull(); // DB에 의해 ID가 생성되었는지 확인

    Optional<User> foundUserOpt = userRepository.findById(savedUser.getId());

    // then: 조회된 User 검증
    assertThat(foundUserOpt).isPresent();
    foundUserOpt.ifPresent(foundUser -> {
      assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
      assertThat(foundUser.getNickname()).isEqualTo(user.getNickname());
      assertThat(foundUser.getPassword()).isEqualTo(user.getPassword());
      assertThat(foundUser.isDeleted()).isFalse();
      assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
      assertThat(foundUser.getCreatedAt()).isNotNull(); // @CreatedDate 작동 확인
    });
  }

  @Test
  @DisplayName("성공: 존재하는 이메일로 조회 시 해당 User를 Optional로 반환한다.")
  void findByEmail_whenEmailExists_returnsUser() {
    // given: 테스트할 User 저장
    String targetEmail = "findme@example.com";
    User user = User.builder().email(targetEmail).nickname("findUser").password("passwordFind").build();
    userRepository.save(user);

    // when: 해당 이메일로 조회
    Optional<User> foundUserOpt = userRepository.findByEmail(targetEmail);

    // then: 조회 결과 검증
    assertThat(foundUserOpt).isPresent();
    assertThat(foundUserOpt.get().getEmail()).isEqualTo(targetEmail);
    assertThat(foundUserOpt.get().getNickname()).isEqualTo("findUser");
  }

  @Test
  @DisplayName("성공: 존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다.")
  void findByEmail_whenEmailDoesNotExist_returnsEmpty() {
    // given: 존재하지 않는 이메일 정의 및 다른 사용자 저장
    String nonExistentEmail = "nobody@example.com";
    userRepository.save(User.builder().email("other@example.com").nickname("other").password("pwd").build());

    // when: 존재하지 않는 이메일로 조회
    Optional<User> foundUserOpt = userRepository.findByEmail(nonExistentEmail);

    // then: 조회 결과가 비어있는지 검증
    assertThat(foundUserOpt).isEmpty();
  }

  @Test
  @DisplayName("성공: 존재하는 이메일로 검사 시 true를 반환한다.")
  void existsByEmail_whenEmailExists_returnsTrue() {
    // given: 테스트할 User 저장
    String targetEmail = "exists@example.com";
    User user = User.builder().email(targetEmail).nickname("existsUser").password("passwordExists").build();
    userRepository.save(user);

    // when: 해당 이메일 존재 여부 확인
    boolean exists = userRepository.existsByEmail(targetEmail);

    // then: 결과가 true인지 검증
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("성공: 존재하지 않는 이메일로 검사 시 false를 반환한다.")
  void existsByEmail_whenEmailDoesNotExist_returnsFalse() {
    // given: 존재하지 않는 이메일 정의 및 다른 사용자 저장
    String nonExistentEmail = "nosuchuser@example.com";
    userRepository.save(User.builder().email("other@example.com").nickname("other").password("pwd").build());

    // when: 존재하지 않는 이메일 존재 여부 확인
    boolean exists = userRepository.existsByEmail(nonExistentEmail);

    // then: 결과가 false인지 검증
    assertThat(exists).isFalse();
  }
}
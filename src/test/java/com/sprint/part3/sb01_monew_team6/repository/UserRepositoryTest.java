package com.sprint.part3.sb01_monew_team6.repository; // repository 패키지 사용 예시

import com.sprint.part3.sb01_monew_team6.entity.User; // User 엔티티 임포트
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest; // DataJpaTest 임포트

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 테스트 (기본적으로 H2 인메모리 DB 사용, 트랜잭션 자동 롤백)
class UserRepositoryTest {

  @Autowired // 테스트 대상인 UserRepository 주입 시도 (아직 인터페이스 없음 -> 에러 발생)
  private UserRepository userRepository;

  @Test
  @DisplayName("사용자를 저장하고 ID로 조회하면 해당 사용자가 반환되어야 한다.")
  void saveAndFindById_returnsSavedUser() {
    // given: 저장할 User 객체 생성 (Builder 사용)
    User user = User.builder()
        .email("repo@example.com")
        .nickname("repoUser")
        .password("hashedPasswordRepo")
        .build();

    // when: User 저장 및 ID로 조회 시도 (아직 userRepository 변수/타입 없음 -> 컴파일 에러 또는 NullPointerException)
     User savedUser = userRepository.save(user);
     Optional<User> foundUserOpt = userRepository.findById(savedUser.getId());

    // then: 조회된 User 검증 (아직 userRepository 없음 -> 컴파일 에러 또는 NullPointerException)
     assertThat(foundUserOpt).isPresent();
     assertThat(foundUserOpt.get().getEmail()).isEqualTo(user.getEmail());
     assertThat(foundUserOpt.get().getNickname()).isEqualTo(user.getNickname());
     assertThat(foundUserOpt.get().getPassword()).isEqualTo(user.getPassword());
     assertThat(foundUserOpt.get().isDeleted()).isFalse();
     assertThat(foundUserOpt.get().getId()).isNotNull(); // 저장 후에는 ID가 있어야 함
     assertThat(foundUserOpt.get().getCreatedAt()).isNotNull(); // 저장 후에는 createdAt이 있어야 함 (Auditing)

    // --- TDD Red 단계: 실패 확인 ---
    //fail("1단계: UserRepository 인터페이스가 아직 정의되지 않았습니다.");
  }
  @Test
  @DisplayName("존재하는 이메일로 조회 시 해당 User를 Optional로 반환한다.")
  void findByEmail_whenEmailExists_returnsUser() {
    // given: 테스트할 User 저장
    String targetEmail = "findme@example.com";
    User user = User.builder()
        .email(targetEmail)
        .nickname("findUser")
        .password("passwordFind")
        .build();
    userRepository.save(user);

    // when: 해당 이메일로 조회
    Optional<User> foundUserOpt = userRepository.findByEmail(targetEmail); // 이제 동작함

    // then: 조회 결과 검증
    assertThat(foundUserOpt).isPresent(); // Optional이 비어있지 않음
    assertThat(foundUserOpt.get().getEmail()).isEqualTo(targetEmail); // Optional 내부 객체의 이메일 확인
    assertThat(foundUserOpt.get().getNickname()).isEqualTo("findUser"); // 추가 검증
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다.")
  void findByEmail_whenEmailDoesNotExist_returnsEmpty() {
    // given: 존재하지 않는 이메일
    String nonExistentEmail = "nobody@example.com";
    // 다른 사용자를 저장해도 결과는 같음
    userRepository.save(User.builder().email("other@example.com").nickname("other").password("pwd").build());


    // when: 존재하지 않는 이메일로 조회
    Optional<User> foundUserOpt = userRepository.findByEmail(nonExistentEmail); // 이제 동작함

    // then: 조회 결과가 비어있는지 검증
    assertThat(foundUserOpt).isEmpty(); // Optional이 비어있음
  }

  @Test
  @DisplayName("존재하는 이메일로 검사 시 true를 반환한다.")
  void existsByEmail_whenEmailExists_returnsTrue() {
    // given: 테스트할 User 저장
    String targetEmail = "exists@example.com";
    User user = User.builder()
        .email(targetEmail)
        .nickname("existsUser")
        .password("passwordExists")
        .build();
    userRepository.save(user);

    // when: 해당 이메일 존재 여부 확인
    boolean exists = userRepository.existsByEmail(targetEmail); // 이제 동작함

    // then: 결과가 true인지 검증
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 검사 시 false를 반환한다.")
  void existsByEmail_whenEmailDoesNotExist_returnsFalse() {
    // given: 존재하지 않는 이메일
    String nonExistentEmail = "nosuchuser@example.com";
    userRepository.save(User.builder().email("other@example.com").nickname("other").password("pwd").build());

    // when: 존재하지 않는 이메일 존재 여부 확인
    boolean exists = userRepository.existsByEmail(nonExistentEmail); // 이제 동작함

    // then: 결과가 false인지 검증
    assertThat(exists).isFalse();
  }
}

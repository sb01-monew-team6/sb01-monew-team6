package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.config.JpaConfig;
import com.sprint.part3.sb01_monew_team6.config.QueryDslConfig;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant; // Instant 임포트
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaConfig.class, QueryDslConfig.class})
class InterestRepositoryTest {

  @Container
  static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
      .withDatabaseName("test_monew")
      .withUsername("testuser")
      .withPassword("testpass")
      .withInitScript("schema.sql");

  @DynamicPropertySource
  static void postgresqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
  }

  @Autowired
  private InterestRepository interestRepository;

  private static final String KEYWORD_DELIMITER = ",";

  @Test
  @DisplayName("성공: 관심사를 저장하고 ID로 조회하면 해당 관심사가 반환된다.")
  void saveAndFindById_returnsSavedInterest() {
    // given
    String keywordsString = "IT" + KEYWORD_DELIMITER + "개발" + KEYWORD_DELIMITER + "스타트업";
    Interest interest = Interest.builder()
        .name("테크")
        .keywords(keywordsString) // 문자열로 전달
        .build();
    // when
    Interest savedInterest = interestRepository.save(interest);
    Optional<Interest> foundInterestOpt = interestRepository.findById(savedInterest.getId());

    // then
    assertThat(foundInterestOpt).isPresent();
    foundInterestOpt.ifPresent(foundInterest -> {
      assertThat(foundInterest.getId()).isNotNull();
      assertThat(foundInterest.getName()).isEqualTo(interest.getName());
      assertThat(foundInterest.getKeywords()).isEqualTo(keywordsString); // 문자열 비교
      assertThat(foundInterest.getSubscriberCount()).isZero();
      assertThat(foundInterest.getCreatedAt()).isNotNull();
    });
  }

  @Test
  @DisplayName("성공: 존재하는 이름으로 조회 시 Interest 반환")
  void findByName_whenNameExists_returnsInterest() {
    // given
    String targetName = "정치";
    Interest interest = Interest.builder().name(targetName).keywords("국회,선거").build(); // 문자열로 전달
    interestRepository.save(interest);

    // when
    Optional<Interest> foundInterestOpt = interestRepository.findByName(targetName);

    // then
    assertThat(foundInterestOpt).isPresent();
    assertThat(foundInterestOpt.get().getName()).isEqualTo(targetName);
    assertThat(foundInterestOpt.get().getKeywords()).isEqualTo("국회,선거"); // 문자열 비교
  }

  @Test
  @DisplayName("성공: 존재하지 않는 이름으로 조회 시 빈 Optional 반환")
  void findByName_whenNameDoesNotExist_returnsEmpty() {
    // given
    String nonExistentName = "외계인";
    interestRepository.save(Interest.builder().name("경제").keywords("주식").build()); // 문자열로 전달

    // when
    Optional<Interest> foundInterestOpt = interestRepository.findByName(nonExistentName);

    // then
    assertThat(foundInterestOpt).isEmpty();
  }

  @Test
  @DisplayName("성공: 존재하는 이름으로 검사 시 true 반환")
  void existsByName_whenNameExists_returnsTrue() {
    // given
    String targetName = "사회";
    Interest interest = Interest.builder().name(targetName).build(); // keywords는 null로 설정됨
    interestRepository.save(interest);

    // when
    boolean exists = interestRepository.existsByName(targetName);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("성공: 존재하지 않는 이름으로 검사 시 false 반환")
  void existsByName_whenNameDoesNotExist_returnsFalse() {
    // given
    String nonExistentName = "UFO";
    interestRepository.save(Interest.builder().name("국제").build()); // keywords는 null로 설정됨

    // when
    boolean exists = interestRepository.existsByName(nonExistentName);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("성공(QueryDSL): 커서 ID 기반 페이지네이션 (ID 오름차순) 테스트")
  void searchWithCursor_whenSortedByIdAsc_returnsCorrectSlice() {
    // given: 여러 개의 Interest 저장
    Interest interest1 = interestRepository.save(
        Interest.builder().name("A_Interest").keywords("a").build()); // 문자열
    Interest interest2 = interestRepository.save(
        Interest.builder().name("C_Interest").keywords("c").build()); // 문자열
    Interest interest3 = interestRepository.save(
        Interest.builder().name("B_Interest").keywords("b").build()); // 문자열
    Interest interest4 = interestRepository.save(
        Interest.builder().name("D_Interest").keywords("d").build()); // 문자열

    int pageSize = 2;
    Pageable pageableIdAsc = PageRequest.of(0, pageSize, Sort.by("id").ascending());

    // when 1: 첫 페이지 조회 (cursorId = null, cursorValue = null)
    Slice<Interest> firstSlice = interestRepository.searchWithCursor(null, pageableIdAsc, null, null);

    // then 1: 첫 페이지 결과 검증
    assertThat(firstSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(firstSlice.hasNext()).isTrue();
    assertThat(firstSlice.getContent()).extracting(Interest::getId)
        .containsExactly(interest1.getId(), interest2.getId());
    Long lastIdFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getId();

    // when 2: 두 번째 페이지 조회 (ID 정렬이므로 cursorValue는 ID 값)
    Slice<Interest> secondSlice = interestRepository.searchWithCursor(null, pageableIdAsc,
        lastIdFromFirstSlice, lastIdFromFirstSlice);

    // then 2: 두 번째 페이지 결과 검증
    assertThat(secondSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(secondSlice.hasNext()).isFalse();
    assertThat(secondSlice.getContent()).extracting(Interest::getId)
        .containsExactly(interest3.getId(), interest4.getId());
  }

  @Test
  @DisplayName("성공(QueryDSL): 검색어로 name 또는 keywords 문자열을 조건 검색할 수 있다")
  void searchWithCursor_keywordFilteringByNameOrKeywordsString() {
    // given
    Interest tech = interestRepository.save(
        Interest.builder().name("Tech News").keywords("IT,스타트업,개발").build()); // 문자열
    Interest health = interestRepository.save(
        Interest.builder().name("Health").keywords("의료,건강").build()); // 문자열
    Interest startup = interestRepository.save(
        Interest.builder().name("Startup Stories").keywords("기업,투자,스타트업").build()); // 문자열
    Interest global = interestRepository.save(
        Interest.builder().name("Global Tech").keywords("해외,기술").build()); // 문자열

    Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

    // when 1: '스타트업' 키워드로 검색
    Slice<Interest> startupResults = interestRepository.searchWithCursor("스타트업", pageable, null, null);

    // then 1: '스타트업' 키워드를 가진 관심사들이 조회되어야 함
    assertThat(startupResults.getContent())
        .extracting(Interest::getName)
        .containsExactly("Startup Stories", "Tech News");

    // when 2: 'Tech' 키워드로 검색
    Slice<Interest> techResults = interestRepository.searchWithCursor("Tech", pageable, null, null);

    // then 2: 'Tech' 키워드를 가진 관심사들이 조회되어야 함
    assertThat(techResults.getContent())
        .extracting(Interest::getName)
        .containsExactly("Global Tech", "Tech News");

    // when 3: '건강' 키워드로 검색
    Slice<Interest> healthResults = interestRepository.searchWithCursor("건강", pageable, null, null);

    // then 3: '건강' 키워드를 가진 관심사가 조회되어야 함
    assertThat(healthResults.getContent())
        .extracting(Interest::getName)
        .containsExactly("Health");

    // when 4: 존재하지 않는 키워드로 검색
    Slice<Interest> nonExistentResults = interestRepository.searchWithCursor("우주", pageable, null, null);

    // then 4: 결과가 없어야 함
    assertThat(nonExistentResults.getContent()).isEmpty();
  }

  @Test
  @DisplayName("성공(GREEN): 이름(name) 기준 오름차순 정렬 시 커서 페이지네이션")
  void searchWithCursor_whenSortedByNameAsc_returnsCorrectSlice() {
    // given: 이름 순서가 ID 순서와 다른 데이터 저장
    Interest interestB = interestRepository.save(Interest.builder().name("B_Interest").keywords("b").build());
    Interest interestA = interestRepository.save(Interest.builder().name("A_Interest").keywords("a").build());
    Interest interestD = interestRepository.save(Interest.builder().name("D_Interest").keywords("d").build());
    Interest interestC = interestRepository.save(Interest.builder().name("C_Interest").keywords("c").build());

    int pageSize = 2;
    Pageable pageableNameAsc = PageRequest.of(0, pageSize, Sort.by("name").ascending().and(Sort.by("id").ascending()));

    // when 1: 첫 페이지 조회
    Slice<Interest> firstSlice = interestRepository.searchWithCursor(null, pageableNameAsc, null, null);

    // then 1: 첫 페이지 결과 검증
    assertThat(firstSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(firstSlice.hasNext()).isTrue();
    assertThat(firstSlice.getContent()).extracting(Interest::getName)
        .containsExactly("A_Interest", "B_Interest");

    Long lastIdFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getId();
    String lastNameFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getName();

    // when 2: 두 번째 페이지 조회
    Slice<Interest> secondSlice = interestRepository.searchWithCursor(null, pageableNameAsc,
        lastIdFromFirstSlice, lastNameFromFirstSlice);

    // then 2: 두 번째 페이지 결과 검증
    assertThat(secondSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(secondSlice.hasNext()).isFalse();
    assertThat(secondSlice.getContent()).extracting(Interest::getName)
        .as("이름 오름차순 정렬 시 두 번째 페이지 결과 검증")
        .containsExactly("C_Interest", "D_Interest");
  }

  @Test
  @DisplayName("성공(GREEN): 이름(name) 기준 내림차순 정렬 시 커서 페이지네이션")
  void searchWithCursor_whenSortedByNameDesc_returnsCorrectSlice() {
    // given
    Interest interestB = interestRepository.save(Interest.builder().name("B_Interest").keywords("b").build());
    Interest interestA = interestRepository.save(Interest.builder().name("A_Interest").keywords("a").build());
    Interest interestD = interestRepository.save(Interest.builder().name("D_Interest").keywords("d").build());
    Interest interestC = interestRepository.save(Interest.builder().name("C_Interest").keywords("c").build());

    int pageSize = 2;
    Pageable pageableNameDesc = PageRequest.of(0, pageSize, Sort.by("name").descending().and(Sort.by("id").ascending()));

    // when 1: 첫 페이지 조회
    Slice<Interest> firstSlice = interestRepository.searchWithCursor(null, pageableNameDesc, null, null);

    // then 1: 첫 페이지 결과 검증
    assertThat(firstSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(firstSlice.hasNext()).isTrue();
    assertThat(firstSlice.getContent()).extracting(Interest::getName)
        .containsExactly("D_Interest", "C_Interest");

    Long lastIdFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getId();
    String lastNameFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getName();

    // when 2: 두 번째 페이지 조회
    Slice<Interest> secondSlice = interestRepository.searchWithCursor(null, pageableNameDesc,
        lastIdFromFirstSlice, lastNameFromFirstSlice);

    // then 2: 두 번째 페이지 결과 검증
    assertThat(secondSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(secondSlice.hasNext()).isFalse();
    assertThat(secondSlice.getContent()).extracting(Interest::getName)
        .as("이름 내림차순 정렬 시 두 번째 페이지 결과 검증")
        .containsExactly("B_Interest", "A_Interest");
  }

  @Test
  @DisplayName("성공(GREEN): 구독자 수(subscriberCount) 기준 오름차순 정렬 시 커서 페이지네이션")
  void searchWithCursor_whenSortedBySubscriberCountAsc_returnsCorrectSlice() {
    // given
    Interest interest10 = interestRepository.save(Interest.builder().name("Interest_10").keywords("").subscriberCount(10L).build());
    Interest interest5_1 = interestRepository.save(Interest.builder().name("Interest_5_1").keywords("").subscriberCount(5L).build());
    Interest interest20 = interestRepository.save(Interest.builder().name("Interest_20").keywords("").subscriberCount(20L).build());
    Interest interest5_2 = interestRepository.save(Interest.builder().name("Interest_5_2").keywords("").subscriberCount(5L).build());

    int pageSize = 2;
    Pageable pageableSubCntAsc = PageRequest.of(0, pageSize, Sort.by("subscriberCount").ascending().and(Sort.by("id").ascending()));

    // when 1: 첫 페이지 조회
    Slice<Interest> firstSlice = interestRepository.searchWithCursor(null, pageableSubCntAsc, null, null);

    // then 1: 첫 페이지 결과 검증
    assertThat(firstSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(firstSlice.hasNext()).isTrue();
    assertThat(firstSlice.getContent()).extracting(Interest::getName)
        .containsExactly("Interest_5_1", "Interest_5_2");

    Long lastIdFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getId();
    Long lastSubCntFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getSubscriberCount();

    // when 2: 두 번째 페이지 조회
    Slice<Interest> secondSlice = interestRepository.searchWithCursor(null, pageableSubCntAsc,
        lastIdFromFirstSlice, lastSubCntFromFirstSlice);

    // then 2: 두 번째 페이지 결과 검증
    assertThat(secondSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(secondSlice.hasNext()).isFalse();
    assertThat(secondSlice.getContent()).extracting(Interest::getName)
        .as("구독자 수 오름차순 정렬 시 두 번째 페이지 결과 검증")
        .containsExactly("Interest_10", "Interest_20");
  }

  @Test
  @DisplayName("성공(GREEN): 생성 시각(createdAt) 기준 오름차순 정렬 시 커서 페이지네이션")
  void searchWithCursor_whenSortedByCreatedAtAsc_returnsCorrectSlice() throws InterruptedException {
    // given: 생성 시각 순서가 ID 순서와 다르게 데이터 저장
    Interest interest2 = interestRepository.save(Interest.builder().name("Interest_2").build());
    Thread.sleep(10); // 시간차
    Interest interest4 = interestRepository.save(Interest.builder().name("Interest_4").build());
    Thread.sleep(10);
    Interest interest1 = interestRepository.save(Interest.builder().name("Interest_1").build());
    Thread.sleep(10);
    Interest interest3 = interestRepository.save(Interest.builder().name("Interest_3").build());

    int pageSize = 2;
    Pageable pageableCreatedAtAsc = PageRequest.of(0, pageSize, Sort.by("createdAt").ascending().and(Sort.by("id").ascending()));

    // when 1: 첫 페이지 조회
    Slice<Interest> firstSlice = interestRepository.searchWithCursor(null, pageableCreatedAtAsc, null, null);

    // then 1: 첫 페이지 결과 검증
    assertThat(firstSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(firstSlice.hasNext()).isTrue();
    assertThat(firstSlice.getContent()).extracting(Interest::getName)
        .containsExactly("Interest_2", "Interest_4");

    Long lastIdFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getId();
    Instant lastCreatedAtFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getCreatedAt();

    // when 2: 두 번째 페이지 조회
    Slice<Interest> secondSlice = interestRepository.searchWithCursor(null, pageableCreatedAtAsc,
        lastIdFromFirstSlice, lastCreatedAtFromFirstSlice);

    // then 2: 두 번째 페이지 결과 검증
    assertThat(secondSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(secondSlice.hasNext()).isFalse();
    assertThat(secondSlice.getContent()).extracting(Interest::getName)
        .as("createdAt 오름차순 정렬 시 두 번째 페이지 결과 검증")
        .containsExactly("Interest_1", "Interest_3");
  }

  @Test
  @DisplayName("성공(GREEN): 생성 시각(createdAt) 기준 내림차순 정렬 시 커서 페이지네이션")
  void searchWithCursor_whenSortedByCreatedAtDesc_returnsCorrectSlice() throws InterruptedException {
    // given
    Interest interest2 = interestRepository.save(Interest.builder().name("Interest_2").build());
    Thread.sleep(10);
    Interest interest4 = interestRepository.save(Interest.builder().name("Interest_4").build());
    Thread.sleep(10);
    Interest interest1 = interestRepository.save(Interest.builder().name("Interest_1").build());
    Thread.sleep(10);
    Interest interest3 = interestRepository.save(Interest.builder().name("Interest_3").build());

    int pageSize = 2;
    Pageable pageableCreatedAtDesc = PageRequest.of(0, pageSize, Sort.by("createdAt").descending().and(Sort.by("id").ascending()));

    // when 1: 첫 페이지 조회
    Slice<Interest> firstSlice = interestRepository.searchWithCursor(null, pageableCreatedAtDesc, null, null);

    // then 1: 첫 페이지 결과 검증
    assertThat(firstSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(firstSlice.hasNext()).isTrue();
    assertThat(firstSlice.getContent()).extracting(Interest::getName)
        .containsExactly("Interest_3", "Interest_1");

    Long lastIdFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getId();
    Instant lastCreatedAtFromFirstSlice = firstSlice.getContent().get(pageSize - 1).getCreatedAt();

    // when 2: 두 번째 페이지 조회
    Slice<Interest> secondSlice = interestRepository.searchWithCursor(null, pageableCreatedAtDesc,
        lastIdFromFirstSlice, lastCreatedAtFromFirstSlice);

    // then 2: 두 번째 페이지 결과 검증
    assertThat(secondSlice.getNumberOfElements()).isEqualTo(pageSize);
    assertThat(secondSlice.hasNext()).isFalse();
    assertThat(secondSlice.getContent()).extracting(Interest::getName)
        .as("createdAt 내림차순 정렬 시 두 번째 페이지 결과 검증")
        .containsExactly("Interest_4", "Interest_2");
  }
}

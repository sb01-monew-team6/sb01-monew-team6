package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.Subscription;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNameTooSimilarException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.SubscriptionRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.InterestServiceImpl;
import com.sprint.part3.sb01_monew_team6.service.impl.SubscriptionServiceImpl;
import org.apache.commons.text.similarity.LevenshteinDistance; // InterestService 로직에서 사용
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils; // InterestService 로직에서 사용

import java.time.Instant; // InterestService 로직에서 사용
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private SubscriptionServiceImpl subscriptionService; // 구독 서비스 테스트 대상

  // InterestService 테스트를 위한 설정 (실제로는 별도 테스트 클래스 권장)
  @InjectMocks
  private InterestServiceImpl interestService;

  private static final String KEYWORD_DELIMITER = ",";

  // Helper method to convert List<String> to delimited String
  private String convertKeywordsToString(List<String> keywords) {
    if (keywords == null || keywords.isEmpty()) {
      return null;
    }
    return keywords.stream()
        .filter(StringUtils::hasText) // null 또는 공백 문자열 제외
        .collect(Collectors.joining(KEYWORD_DELIMITER));
  }

  // --- InterestService 관련 테스트 메서드들 ---
  @Test
  @DisplayName("신규 관심사 이름 등록 성공 시 저장 및 반환")
  void createInterest_whenNameIsNewAndNotSimilar_savesAndReturnsInterest() {
    // given
    String name = "여행";
    List<String> keywordsList = List.of("해외", "국내", "숙소");
    String expectedKeywordsString = convertKeywordsToString(keywordsList);

    when(interestRepository.existsByName(name)).thenReturn(false);
    when(interestRepository.findAll()).thenReturn(Collections.emptyList()); // 유사도 검사를 위해 findAll Mocking
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> {
      Interest interestToSave = invocation.getArgument(0);
      ReflectionTestUtils.setField(interestToSave, "id", 1L); // 테스트 편의상 ID 설정
      return interestToSave;
    });

    // when: 서비스 메소드 호출 (List<String> 전달)
    Interest createdInterest = interestService.createInterest(name, keywordsList);

    // then: 반환값 검증
    assertThat(createdInterest).isNotNull();
    assertThat(createdInterest.getName()).isEqualTo(name);
    assertThat(createdInterest.getKeywords()).isEqualTo(expectedKeywordsString);

    // verify: repository 상호작용 검증
    verify(interestRepository).findAll(); // 유사도 검사 호출 확인
    verify(interestRepository).existsByName(name); // 이름 중복 검사 호출 확인
    ArgumentCaptor<Interest> interestCaptor = ArgumentCaptor.forClass(Interest.class);
    verify(interestRepository).save(interestCaptor.capture());
    assertThat(interestCaptor.getValue().getName()).isEqualTo(name);
    assertThat(interestCaptor.getValue().getKeywords()).isEqualTo(expectedKeywordsString);
  }

  @Test
  @DisplayName("이미 존재하는 이름으로 관심사 등록 시 InterestAlreadyExistsException 발생")
  void createInterest_whenNameExists_throwsInterestAlreadyExistsException() {
    // given
    String existingName = "기존 관심사";
    List<String> keywordsList = List.of("키워드1", "키워드2");

    when(interestRepository.existsByName(existingName)).thenReturn(true);
    // 유사도 검사 로직 때문에 findAll()도 Mocking 필요
    when(interestRepository.findAll()).thenReturn(List.of(Interest.builder().name(existingName).build()));

    // when & then
    assertThatThrownBy(() -> interestService.createInterest(existingName, keywordsList))
        .isInstanceOf(InterestAlreadyExistsException.class)
        .hasMessage(INTEREST_ALREADY_EXISTS.getMessage());

    verify(interestRepository).findAll(); // 유사도 검사 호출 확인
    verify(interestRepository).existsByName(existingName); // 이름 중복 검사 호출 확인
    verify(interestRepository, never()).save(any(Interest.class));
  }

  @Test
  @DisplayName("키워드 수정 성공")
  void updateKeywords_whenInterestExists_updatesAndReturnsInterest() {
    // given: 기존 관심사 ID, 새로운 키워드 목록, Mock Repository 설정
    Long interestId = 1L;
    List<String> newKeywordsList = List.of("new1", "new2", "new3");
    String expectedNewKeywordsString = convertKeywordsToString(newKeywordsList);

    Interest existingInterest = spy(Interest.builder().name("Old Name").keywords("old1").build());
    ReflectionTestUtils.setField(existingInterest, "id", interestId);

    when(interestRepository.findById(eq(interestId))).thenReturn(Optional.of(existingInterest));
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when: 키워드 수정 서비스 메소드 호출
    Interest updatedInterest = interestService.updateInterestKeywords(interestId, newKeywordsList);

    // then: Mock 상호작용 검증
    verify(interestRepository).findById(eq(interestId));
    verify(existingInterest).updateKeywords(eq(expectedNewKeywordsString));
    verify(interestRepository).save(eq(existingInterest));
    assertThat(updatedInterest).isEqualTo(existingInterest);
    assertThat(updatedInterest.getKeywords()).isEqualTo(expectedNewKeywordsString);
  }

  @Test
  @DisplayName("너무 유사한 이름으로 관심사 등록 시 InterestNameTooSimilarException 발생")
  void createInterest_whenNameIsTooSimilar_throwsInterestNameTooSimilarException() {
    // given: 새로운 이름, 키워드, 그리고 '기존'에 유사한 이름의 관심사가 있다고 가정
    String newName = "스포츠뉴스";
    List<String> keywordsList = List.of("종합", "결과");
    String existingSimilarName = "스포츠 뉴스";

    Interest existingInterest = Interest.builder().name(existingSimilarName).keywords("기존").build();
    when(interestRepository.findAll()).thenReturn(Arrays.asList(existingInterest));
    // --- vvv 불필요한 existsByName Mocking 제거 vvv ---
    // when(interestRepository.existsByName(newName)).thenReturn(false);
    // --- ^^^ 불필요한 existsByName Mocking 제거 ^^^ ---

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> interestService.createInterest(newName, keywordsList))
        .isInstanceOf(InterestNameTooSimilarException.class)
        .hasMessage(INTEREST_NAME_TOO_SIMILAR.getMessage());

    // 검증: findAll은 호출되지만, existsByName 및 save는 호출되지 않음
    verify(interestRepository).findAll();
    // --- vvv 불필요한 existsByName 검증 제거 vvv ---
    // verify(interestRepository).existsByName(newName);
    // --- ^^^ 불필요한 existsByName 검증 제거 ^^^ ---
    verify(interestRepository, never()).save(any(Interest.class));
  }

  @Test
  @DisplayName("존재하는 관심사 ID로 삭제 요청 시 Repository의 deleteById 호출")
  void deleteInterest_whenInterestExists_callsDeleteById() {
    // given
    Long existingInterestId = 1L;
    when(interestRepository.existsById(existingInterestId)).thenReturn(true);

    // when: 서비스의 삭제 메서드 호출
    interestService.deleteInterest(existingInterestId);

    // then: Repository의 deleteById가 올바른 ID로 호출되었는지 검증
    verify(interestRepository).existsById(existingInterestId);
    verify(interestRepository).deleteById(existingInterestId);
  }

  @Test
  @DisplayName("존재하지 않는 관심사 ID로 삭제 요청 시 InterestNotFoundException 발생")
  void deleteInterest_whenInterestNotFound_throwsInterestNotFoundException() {
    // given
    Long nonExistentInterestId = 999L;
    when(interestRepository.existsById(nonExistentInterestId)).thenReturn(false);

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> interestService.deleteInterest(nonExistentInterestId))
        .isInstanceOf(InterestNotFoundException.class)
        .hasMessage(INTEREST_NOT_FOUND.getMessage());

    // then: Repository의 deleteById는 호출되지 않아야 함
    verify(interestRepository).existsById(nonExistentInterestId);
    verify(interestRepository, never()).deleteById(anyLong());
  }


  // --- SubscriptionService 관련 테스트 메서드들 ---
  @Test
  @DisplayName("사용자와 관심사가 존재하고 구독하지 않은 상태에서 구독 성공")
  void subscribe_whenUserAndInterestExistAndNotSubscribed_shouldSucceed() {
    // given
    Long userId = 1L;
    Long interestId = 10L;

    User mockUser = mock(User.class);
    Interest mockInterest = mock(Interest.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(interestRepository.findById(interestId)).thenReturn(Optional.of(mockInterest));
    when(subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)).thenReturn(false);
    when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
      Subscription subToSave = invocation.getArgument(0);
      ReflectionTestUtils.setField(subToSave, "id", 100L);
      return subToSave;
    });

    // when: 구독 서비스 메서드 호출
    subscriptionService.subscribe(userId, interestId);

    // then: 상호작용 검증
    verify(userRepository).findById(userId);
    verify(interestRepository).findById(interestId);
    verify(subscriptionRepository).existsByUserIdAndInterestId(userId, interestId);
    ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
    verify(subscriptionRepository).save(subscriptionCaptor.capture());
    Subscription savedSubscription = subscriptionCaptor.getValue();
    assertThat(savedSubscription.getUser()).isEqualTo(mockUser);
    assertThat(savedSubscription.getInterest()).isEqualTo(mockInterest);
    verify(mockInterest).incrementSubscriberCount();
  }

  @Test
  @DisplayName("존재하지 않는 사용자로 구독 요청 시 UserNotFoundException 발생")
  void subscribe_whenUserNotFound_throwsUserNotFoundException() {
    // given
    Long nonExistentUserId = 999L;
    Long interestId = 10L;

    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

    // when & then: UserNotFoundException 예외 발생 검증
    assertThatThrownBy(() -> subscriptionService.subscribe(nonExistentUserId, interestId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage(USER_NOT_FOUND.getMessage());

    // then: 다른 Repository 메서드는 호출되지 않아야 함
    verify(userRepository).findById(nonExistentUserId);
    verify(interestRepository, never()).findById(anyLong());
    verify(subscriptionRepository, never()).existsByUserIdAndInterestId(anyLong(), anyLong());
    verify(subscriptionRepository, never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("존재하지 않는 관심사로 구독 요청 시 InterestNotFoundException 발생")
  void subscribe_whenInterestNotFound_throwsInterestNotFoundException() {
    // given
    Long userId = 1L;
    Long nonExistentInterestId = 999L;

    User mockUser = mock(User.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(interestRepository.findById(nonExistentInterestId)).thenReturn(Optional.empty());

    // when & then: InterestNotFoundException 예외 발생 검증
    assertThatThrownBy(() -> subscriptionService.subscribe(userId, nonExistentInterestId))
        .isInstanceOf(InterestNotFoundException.class)
        .hasMessage(INTEREST_NOT_FOUND.getMessage());

    // then: 다른 Repository 메서드는 호출되지 않아야 함
    verify(userRepository).findById(userId);
    verify(interestRepository).findById(nonExistentInterestId);
    verify(subscriptionRepository, never()).existsByUserIdAndInterestId(anyLong(), anyLong());
    verify(subscriptionRepository, never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("이미 구독 중인 관심사에 구독 요청 시 SubscriptionAlreadyExistsException 발생") // 상태 변경
  void subscribe_whenAlreadySubscribed_throwsSubscriptionAlreadyExistsException() {
    // given
    Long userId = 1L;
    Long interestId = 10L;

    User mockUser = mock(User.class);
    Interest mockInterest = mock(Interest.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(interestRepository.findById(interestId)).thenReturn(Optional.of(mockInterest));
    when(subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)).thenReturn(true); // 이미 구독 중

    // when & then: SubscriptionAlreadyExistsException 예외 발생 검증
    assertThatThrownBy(() -> subscriptionService.subscribe(userId, interestId))
        .isInstanceOf(SubscriptionAlreadyExistsException.class)
        .hasMessage(SUBSCRIPTION_ALREADY_EXISTS.getMessage());

    // then: save 메서드는 호출되지 않아야 함
    verify(userRepository).findById(userId);
    verify(interestRepository).findById(interestId);
    verify(subscriptionRepository).existsByUserIdAndInterestId(userId, interestId);
    verify(subscriptionRepository, never()).save(any(Subscription.class));
    verify(mockInterest, never()).incrementSubscriberCount();
  }


  @Test
  @DisplayName("구독 중인 관심사 구독 취소 성공")
  void unsubscribe_whenSubscriptionExists_shouldSucceed() {
    // given
    Long userId = 1L;
    Long interestId = 10L;

    User mockUser = mock(User.class);
    Interest mockInterest = mock(Interest.class);
    Subscription mockSubscription = Subscription.builder()
        .user(mockUser)
        .interest(mockInterest)
        .build();
    ReflectionTestUtils.setField(mockSubscription, "id", 100L);

    when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId))
        .thenReturn(Optional.of(mockSubscription));

    // when: 구독 취소 서비스 메서드 호출
    subscriptionService.unsubscribe(userId, interestId);

    // then: 상호작용 검증
    verify(subscriptionRepository).findByUserIdAndInterestId(userId, interestId);
    verify(subscriptionRepository).delete(eq(mockSubscription));
    verify(mockInterest).decrementSubscriberCount();
  }

  @Test
  @DisplayName("구독하지 않은 관심사 구독 취소 시 SubscriptionNotFoundException 발생")
  void unsubscribe_whenSubscriptionNotFound_throwsSubscriptionNotFoundException() {
    // given
    Long userId = 1L;
    Long interestId = 10L;

    when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId))
        .thenReturn(Optional.empty());

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> subscriptionService.unsubscribe(userId, interestId))
        .isInstanceOf(SubscriptionNotFoundException.class)
        .hasMessage(SUBSCRIPTION_NOT_FOUND.getMessage());

    // then: Repository의 delete 메서드는 호출되지 않아야 함
    verify(subscriptionRepository).findByUserIdAndInterestId(userId, interestId);
    verify(subscriptionRepository, never()).delete(any(Subscription.class));
    verify(interestRepository, never()).findById(anyLong());
  }

}

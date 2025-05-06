package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNameTooSimilarException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.InterestServiceImpl;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode; // 사용 안 함
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors; // Collectors 임포트
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.INTEREST_ALREADY_EXISTS;
import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.INTEREST_NAME_TOO_SIMILAR;
import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.INTEREST_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

  @Mock
  private InterestRepository interestRepository;

  @InjectMocks
  private InterestServiceImpl interestService;

  private static final String KEYWORD_DELIMITER = ",";

  private String convertKeywordsToString(List<String> keywords) {
    if (keywords == null || keywords.isEmpty()) {
      return null;
    }
    return keywords.stream()
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(KEYWORD_DELIMITER));
  }

  @Test
  @DisplayName("성공: 신규 관심사 이름 등록 성공 시 저장 및 반환")
  void createInterest_whenNameIsNewAndNotSimilar_savesAndReturnsInterest() {
    // given
    String name = "여행";
    List<String> keywordsList = List.of("해외", "국내", "숙소");
    String expectedKeywordsString = convertKeywordsToString(keywordsList);

    when(interestRepository.existsByName(name)).thenReturn(false);
    when(interestRepository.findAll()).thenReturn(Collections.emptyList());
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when: 서비스 메소드 호출 (List<String> 전달)
    Interest createdInterest = interestService.createInterest(name, keywordsList);

    // then: 반환값 검증
    assertThat(createdInterest).isNotNull();
    assertThat(createdInterest.getName()).isEqualTo(name);
    assertThat(createdInterest.getKeywords()).isEqualTo(expectedKeywordsString); // 변환된 문자열 검증

    // verify: repository 상호작용 검증
    verify(interestRepository).existsByName(name);
    verify(interestRepository).findAll();
    ArgumentCaptor<Interest> interestCaptor = ArgumentCaptor.forClass(Interest.class);
    verify(interestRepository).save(interestCaptor.capture());
    assertThat(interestCaptor.getValue().getName()).isEqualTo(name);
    assertThat(interestCaptor.getValue().getKeywords()).isEqualTo(expectedKeywordsString); // 저장된 객체 내용 검증
  }

  @Test
  @DisplayName("실패: 이미 존재하는 이름으로 관심사 등록 시 InterestAlreadyExistsException 발생")
  void createInterest_whenNameExists_throwsInterestAlreadyExistsException() {
    // given
    String existingName = "기존 관심사";
    List<String> keywordsList = List.of("키워드1", "키워드2");
    Interest existingInterest = Interest.builder().name(existingName).keywords("키워드1,키워드2").build();

    when(interestRepository.existsByName(existingName)).thenReturn(true);
    // 유사도 검사를 위한 findAll Mocking은 이름 중복 시 필요 없을 수 있으나, 로직 순서상 필요하면 추가
    // when(interestRepository.findAll()).thenReturn(List.of(existingInterest));

    // when & then
    assertThatThrownBy(() -> interestService.createInterest(existingName, keywordsList))
        .isInstanceOf(InterestAlreadyExistsException.class)
        .hasMessage(INTEREST_ALREADY_EXISTS.getMessage());

    verify(interestRepository, never()).save(any(Interest.class));
    // verify(interestRepository).findAll(); // 필요시 findAll 호출 검증
  }

  @Test
  @DisplayName("성공: 키워드 수정 성공") // DisplayName 수정
  void updateKeywords_whenInterestExists_updatesAndReturnsInterest() {
    // given: 기존 관심사 ID, 새로운 키워드 목록, Mock Repository 설정
    Long interestId = 1L;
    List<String> newKeywordsList = List.of("new1", "new2", "new3");
    String expectedNewKeywordsString = convertKeywordsToString(newKeywordsList);

    Interest existingInterest = spy(Interest.builder().name("Old Name").keywords("old1").build()); // spy 사용
    // ID 설정을 위해 ReflectionTestUtils 사용 (또는 setter 추가)
    // org.springframework.test.util.ReflectionTestUtils.setField(existingInterest, "id", interestId);

    when(interestRepository.findById(eq(interestId))).thenReturn(Optional.of(existingInterest));
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when: 키워드 수정 서비스 메소드 호출 (List<String> 전달)
    Interest updatedInterest = interestService.updateInterestKeywords(interestId, newKeywordsList);

    // then: Mock 상호작용 검증
    verify(interestRepository).findById(eq(interestId));
    // 엔티티의 updateKeywords가 올바른 문자열로 호출되었는지 검증
    verify(existingInterest).updateKeywords(eq(expectedNewKeywordsString));
    verify(interestRepository).save(eq(existingInterest));
    assertThat(updatedInterest).isEqualTo(existingInterest); // 반환된 객체 확인
    // assertThat(updatedInterest.getKeywords()).isEqualTo(expectedNewKeywordsString); // 반환된 객체의 필드 직접 검증 (spy 덕분에 가능)
  }

  @Test
  @DisplayName("실패: 너무 유사한 이름으로 관심사 등록 시 InterestNameTooSimilarException 발생")
  void createInterest_whenNameIsTooSimilar_throwsInterestNameTooSimilarException() {
    // given: 새로운 이름, 키워드, 그리고 '기존'에 유사한 이름의 관심사가 있다고 가정
    String newName = "스포츠뉴스";
    List<String> keywordsList = List.of("종합", "결과");
    String existingSimilarName = "스포츠 뉴스";

    Interest existingInterest = Interest.builder().name(existingSimilarName).keywords("기존").build();
    when(interestRepository.findAll()).thenReturn(Arrays.asList(existingInterest));
    // 이름 중복은 없다고 가정 (이 경우는 유사도 검사에서 걸림)
    // when(interestRepository.existsByName(newName)).thenReturn(false); // 불필요

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> interestService.createInterest(newName, keywordsList))
        .isInstanceOf(InterestNameTooSimilarException.class)
        .hasMessage(INTEREST_NAME_TOO_SIMILAR.getMessage());

    // 검증: findAll은 호출되지만, save는 호출되지 않음
    verify(interestRepository).findAll();
    verify(interestRepository, never()).existsByName(anyString());
    verify(interestRepository, never()).save(any(Interest.class));
  }
  @Test
  @DisplayName("성공: 존재하는 관심사 ID로 삭제 요청 시 Repository의 deleteById 호출")
  void deleteInterest_whenInterestExists_callsDeleteById() {
    // given
    Long existingInterestId = 1L;
    // 삭제 대상이 존재하는지 확인하기 위해 existsById 또는 findById Mocking 필요
    when(interestRepository.existsById(existingInterestId)).thenReturn(true);
    // deleteById는 void를 반환하므로, 호출 여부만 검증하면 됨
    // doNothing().when(interestRepository).deleteById(existingInterestId); // 명시적 Mocking은 필수는 아님

    // when: 서비스의 삭제 메서드 호출 (아직 메서드가 없음 - 컴파일 에러 발생 또는 테스트 실패 예상)
    interestService.deleteInterest(existingInterestId); // <<<--- 이 메서드를 InterestService/Impl에 추가해야 함

    // then: Repository의 deleteById가 올바른 ID로 호출되었는지 검증
    verify(interestRepository).existsById(existingInterestId); // 존재 확인 호출 검증
    verify(interestRepository).deleteById(existingInterestId); // 삭제 호출 검증
  }

  @Test
  @DisplayName("실패: 존재하지 않는 관심사 ID로 삭제 요청 시 InterestNotFoundException 발생")
  void deleteInterest_whenInterestNotFound_throwsInterestNotFoundException() {
    // given
    Long nonExistentInterestId = 999L;
    // 삭제 대상이 존재하지 않는다고 Mocking
    when(interestRepository.existsById(nonExistentInterestId)).thenReturn(false);

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> interestService.deleteInterest(nonExistentInterestId)) // <<<--- 이 메서드를 InterestService/Impl에 추가해야 함
        .isInstanceOf(InterestNotFoundException.class)
        .hasMessage(INTEREST_NOT_FOUND.getMessage()); // ErrorCode 메시지 검증

    // then: Repository의 deleteById는 호출되지 않아야 함
    verify(interestRepository).existsById(nonExistentInterestId); // 존재 확인 호출 검증
    verify(interestRepository, never()).deleteById(anyLong()); // 삭제는 호출되지 않음
  }
}

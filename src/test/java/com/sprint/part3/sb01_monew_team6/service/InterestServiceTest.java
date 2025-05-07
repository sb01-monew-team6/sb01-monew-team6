package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.InterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestUpdateRequestDto;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNameTooSimilarException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.InterestServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
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
class InterestServiceTest {

  @Mock
  private InterestRepository interestRepository;

  @InjectMocks
  private InterestServiceImpl interestService;

  private static final String KEYWORD_DELIMITER = ",";

  private String convertKeywordsToString(List<String> keywords) {
    if (keywords == null || keywords.isEmpty()) {
      return "";
    }
    return keywords.stream()
        .filter(StringUtils::hasText)
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
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> {
      Interest interestToSave = invocation.getArgument(0);
      ReflectionTestUtils.setField(interestToSave, "id", 1L);
      return interestToSave;
    });

    // when
    Interest createdInterest = interestService.createInterest(name, keywordsList);

    // then
    assertThat(createdInterest).isNotNull();
    assertThat(createdInterest.getName()).isEqualTo(name);
    assertThat(createdInterest.getKeywords()).isEqualTo(expectedKeywordsString);

    verify(interestRepository).existsByName(name);
    verify(interestRepository).findAll();
    ArgumentCaptor<Interest> interestCaptor = ArgumentCaptor.forClass(Interest.class);
    verify(interestRepository).save(interestCaptor.capture());
    assertThat(interestCaptor.getValue().getName()).isEqualTo(name);
    assertThat(interestCaptor.getValue().getKeywords()).isEqualTo(expectedKeywordsString);
  }

  @Test
  @DisplayName("실패: 이미 존재하는 이름으로 관심사 등록 시 InterestAlreadyExistsException 발생")
  void createInterest_whenNameExists_throwsInterestAlreadyExistsException() {
    // given
    String existingName = "기존 관심사";
    List<String> keywordsList = List.of("키워드1", "키워드2");
    when(interestRepository.existsByName(existingName)).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> interestService.createInterest(existingName, keywordsList))
        .isInstanceOf(InterestAlreadyExistsException.class)
        .hasMessage(INTEREST_ALREADY_EXISTS.getMessage());

    // verify: save는 절대 호출되지 않고, existsByName은 호출된다
    verify(interestRepository).existsByName(existingName);
    verify(interestRepository, never()).findAll();
    verify(interestRepository, never()).save(any(Interest.class));
  }

  @Test
  @DisplayName("성공: 키워드 수정 성공")
  void updateKeywords_whenInterestExists_updatesAndReturnsInterest() {
    // given
    Long interestId = 1L;
    List<String> newKeywordsList = List.of("new1", "new2", "new3");
    String expectedNewKeywordsString = convertKeywordsToString(newKeywordsList);

    Interest existingInterest = spy(Interest.builder().name("Old Name").keywords("old1").build());
    ReflectionTestUtils.setField(existingInterest, "id", interestId);
    ReflectionTestUtils.setField(existingInterest, "createdAt", Instant.now());

    when(interestRepository.findById(eq(interestId))).thenReturn(Optional.of(existingInterest));
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> {
      Interest interestToSave = invocation.getArgument(0);
      ReflectionTestUtils.setField(interestToSave, "updatedAt", Instant.now());
      return interestToSave;
    });

    InterestUpdateRequestDto requestDto = new InterestUpdateRequestDto(null, newKeywordsList);

    // when
    Interest updatedInterest = interestService.updateInterest(interestId, requestDto);

    // then
    verify(interestRepository).findById(eq(interestId));
    verify(existingInterest).updateKeywords(eq(expectedNewKeywordsString));
    verify(interestRepository).save(eq(existingInterest));
    assertThat(updatedInterest).isEqualTo(existingInterest);
    assertThat(updatedInterest.getKeywords()).isEqualTo(expectedNewKeywordsString);
    assertThat(updatedInterest.getUpdatedAt()).isNotNull();
  }


  @Test
  @DisplayName("실패: 너무 유사한 이름으로 관심사 등록 시 InterestNameTooSimilarException 발생")
  void createInterest_whenNameIsTooSimilar_throwsInterestNameTooSimilarException() {
    // given
    String newName = "스포츠뉴스";
    List<String> keywordsList = List.of("종합", "결과");
    String existingSimilarName = "스포츠 뉴스";

    Interest existingInterest = Interest.builder().name(existingSimilarName).keywords("기존").build();
    when(interestRepository.existsByName(newName)).thenReturn(false); // 이름 중복은 없음
    when(interestRepository.findAll()).thenReturn(Arrays.asList(existingInterest)); // 유사도 검사에서 걸릴 대상


    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> interestService.createInterest(newName, keywordsList))
        .isInstanceOf(InterestNameTooSimilarException.class)
        .hasMessage(INTEREST_NAME_TOO_SIMILAR.getMessage());

    verify(interestRepository).existsByName(newName); // 이름 중복 검사 호출
    verify(interestRepository).findAll(); // 유사도 검사 호출
    verify(interestRepository, never()).save(any(Interest.class));
  }

  @Test
  @DisplayName("성공: 존재하는 관심사 ID로 삭제 요청 시 Repository의 deleteById 호출")
  void deleteInterest_whenInterestExists_callsDeleteById() {
    // given
    Long existingInterestId = 1L;
    when(interestRepository.existsById(existingInterestId)).thenReturn(true);

    // when
    interestService.deleteInterest(existingInterestId);

    // then
    verify(interestRepository).existsById(existingInterestId);
    verify(interestRepository).deleteById(existingInterestId);
  }

  @Test
  @DisplayName("실패: 존재하지 않는 관심사 ID로 삭제 요청 시 InterestNotFoundException 발생")
  void deleteInterest_whenInterestNotFound_throwsInterestNotFoundException() {
    // given
    Long nonExistentInterestId = 999L;
    when(interestRepository.existsById(nonExistentInterestId)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> interestService.deleteInterest(nonExistentInterestId))
        .isInstanceOf(InterestNotFoundException.class)
        .hasMessage(INTEREST_NOT_FOUND.getMessage());

    verify(interestRepository).existsById(nonExistentInterestId);
    verify(interestRepository, never()).deleteById(anyLong());
  }


  @Test
  @DisplayName("성공(GREEN): 존재하는 관심사의 이름과 키워드 수정 성공")
  void updateInterest_whenInterestExists_updatesNameAndKeywords() {
    // given
    Long interestId = 1L;
    String originalName = "Original Name";
    String originalKeywords = "original,key,words";
    List<String> newKeywordsList = List.of("new", "updated", "keywords");
    String newKeywordsString = convertKeywordsToString(newKeywordsList);
    String newName = "Updated Name";

    InterestUpdateRequestDto requestDto = new InterestUpdateRequestDto(newName, newKeywordsList);

    Interest existingInterest = spy(Interest.builder()
        .name(originalName)
        .keywords(originalKeywords)
        .subscriberCount(5L)
        .build());
    ReflectionTestUtils.setField(existingInterest, "id", interestId);
    Instant initialCreatedAt = Instant.now().minusSeconds(100);
    ReflectionTestUtils.setField(existingInterest, "createdAt", initialCreatedAt);


    when(interestRepository.findById(interestId)).thenReturn(Optional.of(existingInterest));
    // --- vvv Mocking 변경: existsByName -> findByName vvv ---
    when(interestRepository.findByName(newName)).thenReturn(Optional.empty()); // 새 이름으로 조회 시 결과 없음 (중복 아님)
    // --- ^^^ Mocking 변경: existsByName -> findByName ^^^ ---
    when(interestRepository.findAll()).thenReturn(List.of(existingInterest)); // 유사도 검사 시 자기 자신만
    when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> {
      Interest interestToSave = invocation.getArgument(0);
      ReflectionTestUtils.setField(interestToSave, "updatedAt", Instant.now());
      return interestToSave;
    });

    // when
    Interest updatedInterest = interestService.updateInterest(interestId, requestDto);

    // then
    assertThat(updatedInterest).isNotNull();
    assertThat(updatedInterest.getId()).isEqualTo(interestId);
    assertThat(updatedInterest.getName()).isEqualTo(newName);
    assertThat(updatedInterest.getKeywords()).isEqualTo(newKeywordsString);
    assertThat(updatedInterest.getSubscriberCount()).isEqualTo(5L);
    assertThat(updatedInterest.getUpdatedAt()).isNotNull();
    assertThat(updatedInterest.getUpdatedAt()).isAfter(initialCreatedAt);

    verify(existingInterest).setName(eq(newName));
    verify(existingInterest).updateKeywords(eq(newKeywordsString));

    verify(interestRepository).findById(interestId);
    // --- vvv Verify 변경: existsByName -> findByName vvv ---
    verify(interestRepository).findByName(newName); // 이름 중복 검사 호출 확인
    // --- ^^^ Verify 변경: existsByName -> findByName ^^^ ---
    verify(interestRepository).findAll(); // 유사도 검사 호출 확인
    verify(interestRepository).save(existingInterest);
  }

  @Test
  @DisplayName("존재하지 않는 관심사 ID로 수정 요청 시 InterestNotFoundException 발생")
  void updateInterest_whenInterestNotFound_throwsInterestNotFoundException() {
    // given
    Long nonExistentInterestId = 999L;
    InterestUpdateRequestDto requestDto = new InterestUpdateRequestDto("Any Name", List.of("any"));

    when(interestRepository.findById(nonExistentInterestId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> interestService.updateInterest(nonExistentInterestId, requestDto))
        .isInstanceOf(InterestNotFoundException.class)
        .hasMessage(INTEREST_NOT_FOUND.getMessage());

    verify(interestRepository).findById(nonExistentInterestId);
    verify(interestRepository, never()).save(any(Interest.class));
  }

}

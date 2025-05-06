package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNameTooSimilarException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;
import java.util.stream.Collectors; // Collectors 임포트
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;

  private static final LevenshteinDistance LEVENSHTEIN_DISTANCE = new LevenshteinDistance();
  private static final double SIMILARITY_THRESHOLD = 0.8;
  private static final String KEYWORD_DELIMITER = ","; // 키워드 구분자 정의

  @Override
  @Transactional
  public Interest createInterest(String name, List<String> keywords) {
    // 1. 이름 유사도 검사
    checkNameSimilarity(name);

    // 2. 이름 중복 검사
    if (interestRepository.existsByName(name)) {
      throw new InterestAlreadyExistsException(name);
    }

    // --- vvv 키워드 목록을 문자열로 변환 vvv ---
    String keywordsString = convertKeywordsToString(keywords);
    // --- ^^^ 키워드 목록을 문자열로 변환 ^^^ ---

    // 3. Interest 객체 생성
    Interest newInterest = Interest.builder()
        .name(name)
        .keywords(keywordsString) // 변환된 문자열 사용
        .build();

    // 4. 저장 및 반환
    return interestRepository.save(newInterest);
  }

  private void checkNameSimilarity(String newName) {
    // TODO: Refactor - 성능 개선 필요
    List<Interest> existingInterests = interestRepository.findAll();
    for (Interest existingInterest : existingInterests) {
      String existingName = existingInterest.getName();
      if (newName.equals(existingName)) continue;
      int distance = LEVENSHTEIN_DISTANCE.apply(newName, existingName);
      int maxLength = Math.max(newName.length(), existingName.length());
      if (maxLength == 0) continue;
      double similarity = 1.0 - (double) distance / maxLength;
      if (similarity >= SIMILARITY_THRESHOLD) {
        throw new InterestNameTooSimilarException(newName, existingName, similarity);
      }
    }
  }

  @Override
  @Transactional
  public Interest updateInterestKeywords(Long interestId, List<String> newKeywords) {
    Interest interest = findInterestByIdOrThrow(interestId);

    // --- vvv 키워드 목록을 문자열로 변환 vvv ---
    String newKeywordsString = convertKeywordsToString(newKeywords);
    // --- ^^^ 키워드 목록을 문자열로 변환 ^^^ ---

    interest.updateKeywords(newKeywordsString); // 엔티티의 updateKeywords 호출 (String 파라미터)

    return interestRepository.save(interest); // 변경 감지 또는 명시적 저장
  }

  @Override
  @Transactional // 데이터 변경이 있으므로 트랜잭션 적용
  public void deleteInterest(Long interestId) {
    // 1. 관심사 존재 여부 확인
    if (!interestRepository.existsById(interestId)) {
      throw new InterestNotFoundException(interestId); // 없으면 예외 발생
    }
    // 2. 존재하면 삭제 실행
    interestRepository.deleteById(interestId);
  }

  private Interest findInterestByIdOrThrow(Long interestId) {
    return interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(interestId));
  }

  // --- vvv 키워드 List -> String 변환 헬퍼 메서드 vvv ---
  private String convertKeywordsToString(List<String> keywords) {
    if (keywords == null || keywords.isEmpty()) {
      return null; // 또는 빈 문자열 ""
    }
    // 쉼표(,)를 구분자로 사용하여 문자열로 합침
    return keywords.stream()
        .filter(StringUtils::hasText) // 비거나 null인 키워드 제외
        .collect(Collectors.joining(KEYWORD_DELIMITER));
  }
  // --- ^^^ 키워드 List -> String 변환 헬퍼 메서드 ^^^ ---
}

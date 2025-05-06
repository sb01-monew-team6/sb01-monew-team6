package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import java.util.List; // 키워드 입력은 List로 유지

public interface InterestService {

  /**
   * 새로운 관심사를 생성합니다.
   * @param name 관심사 이름
   * @param keywords 관련 키워드 목록 (서비스 내부에서 문자열로 변환)
   * @return 생성된 Interest 엔티티
   * @throws com.sprint.part3.sb01_monew_team6.exception.interest.InterestAlreadyExistsException 이미 존재하는 이름일 때
   * @throws com.sprint.part3.sb01_monew_team6.exception.interest.InterestNameTooSimilarException 유사한 이름이 존재할 때
   */
  Interest createInterest(String name, List<String> keywords); // 입력은 List 유지

  /**
   * 관심사의 키워드를 수정합니다.
   * @param interestId 수정할 관심사 ID
   * @param newKeywords 새로운 키워드 목록 (서비스 내부에서 문자열로 변환)
   * @return 수정된 Interest 엔티티
   * @throws com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException 해당 ID의 관심사가 없을 때
   */
  Interest updateInterestKeywords(Long interestId, List<String> newKeywords); // 입력은 List 유지

  /**
   * 지정된 ID의 관심사를 삭제합니다. (물리 삭제)
   * @param interestId 삭제할 관심사 ID
   * @throws com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException 해당 ID의 관심사가 없을 때
   */
  void deleteInterest(Long interestId);
}

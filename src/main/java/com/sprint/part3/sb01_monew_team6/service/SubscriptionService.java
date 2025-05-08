package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto; // SubscriptionDto 임포트

/**
 * 구독 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface SubscriptionService {

  /**
   * 사용자가 특정 관심사를 구독합니다.
   * @param userId 구독할 사용자 ID
   * @param interestId 구독할 관심사 ID
   * @return 생성된 구독 정보 DTO
   * @throws com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException 사용자를 찾을 수 없을 때
   * @throws com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException 관심사를 찾을 수 없을 때
   * @throws com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionAlreadyExistsException 이미 구독 중일 때
   */
  // --- vvv 반환 타입을 SubscriptionDto로 변경 vvv ---
  SubscriptionDto subscribe(Long userId, Long interestId);
  // --- ^^^ 반환 타입을 SubscriptionDto로 변경 ^^^ ---

  /**
   * 사용자가 특정 관심사 구독을 취소합니다.
   * @param userId 구독 취소할 사용자 ID
   * @param interestId 구독 취소할 관심사 ID
   * @throws com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionNotFoundException 구독 정보를 찾을 수 없을 때
   */
  void unsubscribe(Long userId, Long interestId); // 우선 void 유지

}

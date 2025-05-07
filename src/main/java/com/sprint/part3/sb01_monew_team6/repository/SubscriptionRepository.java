package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // Optional 임포트

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  /**
   * 사용자 ID와 관심사 ID로 구독 정보를 조회합니다.
   * @param userId 사용자 ID
   * @param interestId 관심사 ID
   * @return Optional<Subscription>
   */
  Optional<Subscription> findByUserIdAndInterestId(Long userId, Long interestId);

  /**
   * 사용자 ID와 관심사 ID로 구독 정보 존재 여부를 확인합니다.
   * @param userId 사용자 ID
   * @param interestId 관심사 ID
   * @return boolean 구독 존재 여부
   */
  boolean existsByUserIdAndInterestId(Long userId, Long interestId);


  void deleteByInterestId(Long interestId);
}

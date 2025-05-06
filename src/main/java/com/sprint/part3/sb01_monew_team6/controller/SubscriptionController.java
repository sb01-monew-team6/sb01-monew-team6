package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto; // SubscriptionDto 임포트
import com.sprint.part3.sb01_monew_team6.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal; // 사용 안 함
// import org.springframework.security.core.userdetails.User; // 사용 안 함
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interests/{interestId}") // 공통 경로
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  /**
   * 현재 인증된 사용자가 특정 관심사를 구독합니다.
   * @param interestId 구독할 관심사 ID
   * @param userId 요청 헤더의 사용자 ID
   * @return 성공 시 200 OK 와 SubscriptionDto
   */
  @PostMapping("/subscriptions")
  public ResponseEntity<SubscriptionDto> subscribeInterest( // <<<--- 반환 타입 변경
      @PathVariable Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId // <<<--- 헤더에서 userId 받기
  ) {
    log.info("User {} attempting to subscribe to interest {}", userId, interestId);

    // 서비스 호출하여 구독 처리 및 DTO 받기
    SubscriptionDto subscriptionDto = subscriptionService.subscribe(userId, interestId);

    // 200 OK 와 함께 DTO 반환
    return ResponseEntity.ok(subscriptionDto); // <<<--- 200 OK 및 본문 반환
  }

  /**
   * 현재 인증된 사용자가 특정 관심사 구독을 취소합니다.
   * @param interestId 구독 취소할 관심사 ID
   * @param userId 요청 헤더의 사용자 ID
   * @return 성공 시 204 No Content
   */
  @DeleteMapping("/subscriptions")
  public ResponseEntity<Void> unsubscribeInterest(
      @PathVariable Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId // <<<--- 헤더에서 userId 받기
  ) {
    log.info("User {} attempting to unsubscribe from interest {}", userId, interestId);

    subscriptionService.unsubscribe(userId, interestId);

    // API 명세상 응답 본문이 없으므로 204 No Content 유지
    return ResponseEntity.noContent().build();
  }
}

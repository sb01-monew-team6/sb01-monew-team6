package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto;
import com.sprint.part3.sb01_monew_team6.service.SubscriptionService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests/{interestId}/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @PostMapping
  public ResponseEntity<SubscriptionDto> subscribe(
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") @NotNull Long userId
  ) {
    log.info("User {} subscribing to interest {}", userId, interestId);
    SubscriptionDto dto = subscriptionService.subscribe(userId, interestId);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping
  public ResponseEntity<Void> unsubscribe(
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") @NotNull Long userId
  ) {
    log.info("User {} unsubscribing from interest {}", userId, interestId);
    subscriptionService.unsubscribe(userId, interestId);
    return ResponseEntity.noContent().build();
  }
}

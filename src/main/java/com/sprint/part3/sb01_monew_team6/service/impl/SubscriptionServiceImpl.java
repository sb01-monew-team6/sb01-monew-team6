package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto; // SubscriptionDto 임포트
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
import com.sprint.part3.sb01_monew_team6.service.InterestService; // InterestService 임포트 (다른 메서드에서 사용)
import com.sprint.part3.sb01_monew_team6.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance; // InterestService 로직에서 사용
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // InterestService 로직에서 사용

import java.time.Instant; // InterestService 로직에서 사용
import java.util.List; // InterestService 로직에서 사용
import java.util.stream.Collectors; // InterestService 로직에서 사용

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService { // <<<--- InterestService 구현 제거 (별도 클래스 가정)

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final SubscriptionRepository subscriptionRepository;

  // --- InterestService 관련 필드 및 메서드는 InterestServiceImpl로 이동 가정 ---
  // private static final LevenshteinDistance LEVENSHTEIN_DISTANCE = new LevenshteinDistance();
  // private static final double SIMILARITY_THRESHOLD = 0.8;
  // private static final String KEYWORD_DELIMITER = ",";
  // ... createInterest, updateInterestKeywords, deleteInterest, checkNameSimilarity, findInterestByIdOrThrow, convertKeywordsToString 등 ...


  @Override
  @Transactional
  public SubscriptionDto subscribe(Long userId, Long interestId) { // <<<--- 반환 타입 SubscriptionDto로 변경
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(interestId));

    if (subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)) {
      throw new SubscriptionAlreadyExistsException(userId, interestId);
    }

    Subscription newSubscription = Subscription.builder()
        .user(user)
        .interest(interest)
        .build();
    Subscription savedSubscription = subscriptionRepository.save(newSubscription); // <<<--- 저장된 엔티티 받기

    interest.incrementSubscriberCount();
    // interestRepository.save(interest); // 변경 감지

    log.info("User {} subscribed to Interest {}", userId, interestId);

    return SubscriptionDto.fromEntity(savedSubscription);
  }

  @Override
  @Transactional
  public void unsubscribe(Long userId, Long interestId) {
    Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(userId, interestId)
        .orElseThrow(() -> new SubscriptionNotFoundException(userId, interestId));

    subscriptionRepository.delete(subscription);

    Interest interest = subscription.getInterest();
    interest.decrementSubscriberCount();
    // interestRepository.save(interest); // 변경 감지

    log.info("User {} unsubscribed from Interest {}", userId, interestId);
  }
}

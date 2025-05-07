package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.SubscriptionDto;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.Subscription;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.subscription.SubscriptionNotFoundException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.SubscriptionRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  @Transactional
  public SubscriptionDto subscribe(Long userId, Long interestId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(interestId));

    if (subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)) {
      throw new SubscriptionAlreadyExistsException(userId, interestId);
    }

    Subscription subscription = Subscription.builder()
        .user(user)
        .interest(interest)
        .build();
    // save subscription
    Subscription saved = subscriptionRepository.save(subscription);
    // increment interest count
    interest.incrementSubscriberCount();

    return SubscriptionDto.fromEntity(saved);
  }

  @Override
  @Transactional
  public void unsubscribe(Long userId, Long interestId) {
    Subscription subscription = subscriptionRepository
        .findByUserIdAndInterestId(userId, interestId)
        .orElseThrow(() -> new SubscriptionNotFoundException(userId, interestId));

    // decrement subscriber count
    Interest interest = subscription.getInterest();
    interest.decrementSubscriberCount();

    // delete subscription
    subscriptionRepository.delete(subscription);
  }
}

package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.CursorPageResponseInterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestUpdateRequestDto;
import com.sprint.part3.sb01_monew_team6.repository.SubscriptionRepository;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestAlreadyExistsException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNameTooSimilarException;
import com.sprint.part3.sb01_monew_team6.exception.interest.InterestNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.service.InterestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;
  private final SubscriptionRepository subscriptionRepository;
  private boolean isSubscribed(Long userId, Long interestId) {
    return subscriptionRepository.existsByUserIdAndInterestId(userId, interestId);
  }
  private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();
  private static final double SIMILARITY_THRESHOLD = 0.6;
  private static final String KEYWORD_DELIMITER = ",";


  @Override
  @Transactional
  public Interest createInterest(String name, List<String> keywords) {
    if (interestRepository.existsByName(name)) {
      throw new InterestAlreadyExistsException(name);
    }
    checkNameSimilarity(name, null);
    String kw = convertKeywordsToString(keywords);
    Interest entity = Interest.builder()
        .name(name)
        .keywords(kw)
        .build();
    return interestRepository.save(entity);
  }

  @Override
  @Transactional
  public Interest updateInterest(Long interestId, InterestUpdateRequestDto requestDto) {
    Interest entity = findByIdOrThrow(interestId);
    boolean changed = false;

    // 이름
    if (requestDto.name() != null &&
        !requestDto.name().isBlank() &&
        !Objects.equals(entity.getName(), requestDto.name())) {
      String newName = requestDto.name();
      Optional<Interest> existing = interestRepository.findByName(newName);
      if (existing.isPresent() && !existing.get().getId().equals(interestId)) {
        throw new InterestAlreadyExistsException(newName);
      }
      checkNameSimilarity(newName, interestId);
      entity.setName(newName);
      changed = true;
    }

    // 키워드
    if (requestDto.keywords() != null) {
      String newKw = convertKeywordsToString(requestDto.keywords());
      if (!Objects.equals(entity.getKeywords(), newKw)) {
        entity.updateKeywords(newKw);
        changed = true;
      }
    }

    return changed ? interestRepository.save(entity) : entity;
  }

  @Override
  @Transactional
  public void deleteInterest(Long interestId) {
    if (!interestRepository.existsById(interestId)) {
      throw new InterestNotFoundException(interestId);
    }

    subscriptionRepository.deleteByInterestId(interestId); //  관련 구독 먼저 삭제
    interestRepository.deleteById(interestId); // 관심사 삭제
  }


  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseInterestDto findAll(
      Long userId,
      String keyword,
      Long cursorId,
      String cursorValue,
      String orderBy,
      Sort.Direction direction,
      int limit
  ) {
    Sort sort = Sort.by(direction, orderBy);
    Pageable page = PageRequest.of(0, limit, sort);

    // 1) cursorValue 변환
    Object cursorVal = convertCursorValue(cursorValue, sort);

    // 2) 레포지토리 직접 호출
    var slice = interestRepository.searchWithCursor(keyword, page, cursorId, cursorVal);

    // 3) 엔티티→DTO
    List<InterestDto> dtos = slice.getContent().stream()
        .map(interest -> InterestDto.fromEntity(interest, isSubscribed(userId, interest.getId())))
        .collect(Collectors.toList());

    // 4) nextCursor 계산
    String nextCursor = null, nextAfter = null;
    if (slice.hasNext() && !dtos.isEmpty()) {
      var last = slice.getContent().get(slice.getContent().size() - 1);
      nextCursor = switch (orderBy) {
        case "name"            -> last.getName();
        case "subscriberCount" -> last.getSubscriberCount().toString();
        case "createdAt"       -> last.getCreatedAt().toString();
        default                -> last.getId().toString();
      };
      nextAfter = last.getCreatedAt().toString();
    }

    return new CursorPageResponseInterestDto(
        dtos,
        nextCursor,
        nextAfter,
        slice.getSize(),
        slice.getContent().stream().mapToLong(Interest::getId).count(),
        slice.hasNext()
    );
  }

  // ——— helpers ———

  private Interest findByIdOrThrow(Long id) {
    return interestRepository.findById(id)
        .orElseThrow(() -> new InterestNotFoundException(id));
  }

  private void checkNameSimilarity(String name, Long selfId) {
    for (Interest e : interestRepository.findAll()) {
      if (selfId != null && e.getId().equals(selfId)) continue;
      String other = e.getName();
      if (name.equals(other)) continue;
      int dist = LEVENSHTEIN.apply(name, other);
      int max = Math.max(name.length(), other.length());
      if (max > 0 && 1.0 - (double) dist / max >= SIMILARITY_THRESHOLD) {
        throw new InterestNameTooSimilarException(name, other, 1.0 - (double) dist / max);
      }
    }
  }

  private String convertKeywordsToString(List<String> kws) {
    if (kws == null || kws.isEmpty()) return "";
    return kws.stream().filter(StringUtils::hasText).collect(Collectors.joining(KEYWORD_DELIMITER));
  }

  private Object convertCursorValue(String val, Sort sort) {
    if (val == null || sort.isUnsorted()) return null;
    Sort.Order o = sort.iterator().next();
    return switch (o.getProperty()) {
      case "name" -> val;
      case "subscriberCount" -> Long.parseLong(val);
      case "createdAt" -> Instant.parse(val);
      case "id" -> Long.parseLong(val);
      default -> val;
    };
  }
}

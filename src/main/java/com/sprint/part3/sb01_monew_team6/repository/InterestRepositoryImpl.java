package com.sprint.part3.sb01_monew_team6.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 로깅 추가
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;

import java.time.Instant; // createdAt 처리를 위해 추가
import java.util.ArrayList;
import java.util.List;
// import java.util.function.BiFunction; // 사용 안 함

import static com.sprint.part3.sb01_monew_team6.entity.QInterest.interest;

@RequiredArgsConstructor
@Slf4j // 로깅 어노테이션 추가
public class InterestRepositoryImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Interest> searchWithCursor(String keyword, Pageable pageable, Long cursorId, Object cursorValue) {
    List<Interest> content = queryFactory
        .selectFrom(interest)
        .where(
            keywordSearchCondition(keyword),
            cursorCondition(cursorId, cursorValue, pageable) // 커서 조건 생성
        )
        .orderBy(getOrderSpecifiers(pageable.getSort()).toArray(OrderSpecifier[]::new))
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = false;
    if (content.size() > pageable.getPageSize()) {
      content.remove(pageable.getPageSize());
      hasNext = true;
    }

    return new SliceImpl<>(content, pageable, hasNext);
  }

  /**
   * 이름 또는 keywords 문자열에 키워드가 포함되는지 확인하는 BooleanExpression 생성
   */
  private BooleanExpression keywordSearchCondition(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }
    BooleanExpression nameCond = nameContains(keyword);
    BooleanExpression keywordCond = keywordsStringContains(keyword);

    // BooleanExpression은 null을 반환할 수 있으므로 or 연산 전에 확인
    if (nameCond != null && keywordCond != null) {
      return nameCond.or(keywordCond);
    } else {
      return nameCond != null ? nameCond : keywordCond;
    }
  }

  /**
   * 이름(name) 기준 키워드 검색 조건 생성
   */
  private BooleanExpression nameContains(String keyword) {
    return StringUtils.hasText(keyword) ? interest.name.containsIgnoreCase(keyword) : null;
  }

  /**
   * keywords 문자열 기준 키워드 검색 조건 생성
   */
  private BooleanExpression keywordsStringContains(String keyword) {
    // keywords 필드가 null일 수 있으므로 coalesce 처리 또는 null 체크 필요
    // 여기서는 간단히 containsIgnoreCase 사용 (null이면 false 반환)
    return StringUtils.hasText(keyword) ? interest.keywords.containsIgnoreCase(keyword) : null;
  }


  // --- vvv 커서 조건 생성 메서드 리팩토링 vvv ---
  /**
   * 커서 기반 페이지네이션을 위한 WHERE 조건 생성 (리팩토링)
   * 주 정렬 기준과 ID를 조합하여 복합 커서 조건 생성
   * @param cursorId 마지막 항목의 ID (null 가능, 첫 페이지)
   * @param cursorValue 마지막 항목의 주 정렬 컬럼 값 (null 가능, 첫 페이지 또는 ID 정렬 시)
   * @param pageable 페이지 정보 (정렬 기준 포함)
   * @return BooleanExpression
   */
  private BooleanExpression cursorCondition(Long cursorId, Object cursorValue, Pageable pageable) {
    if (cursorId == null) {
      // 첫 페이지 조회 시 커서 조건 없음
      return null;
    }

    Sort sort = pageable.getSort();
    // 정렬 조건이 없으면 ID 오름차순 기본
    if (sort.isUnsorted()) {
      return interest.id.gt(cursorId);
    }

    // 주 정렬 조건 가져오기 (첫 번째 정렬 조건)
    Sort.Order order = sort.iterator().next();
    String property = order.getProperty();
    boolean isAscending = order.isAscending();

    // ID는 항상 오름차순으로 비교하여 고유 순서 보장 (내림차순 정렬 시에도 ID는 오름차순 비교)
    BooleanExpression idCompareCondition = interest.id.gt(cursorId);

    try {
      switch (property) {
        case "name":
          if (cursorValue == null) return null; // 첫 페이지 이후인데 cursorValue가 null이면 에러 상황, 또는 ID 정렬로 간주? 여기선 null 반환
          String nameCursor = (String) cursorValue;
          if (isAscending) {
            return interest.name.gt(nameCursor).or(interest.name.eq(nameCursor).and(idCompareCondition));
          } else {
            return interest.name.lt(nameCursor).or(interest.name.eq(nameCursor).and(idCompareCondition));
          }
        case "subscriberCount":
          if (cursorValue == null) return null;
          Long subscriberCountCursor = (Long) cursorValue;
          if (isAscending) {
            return interest.subscriberCount.gt(subscriberCountCursor).or(interest.subscriberCount.eq(subscriberCountCursor).and(idCompareCondition));
          } else {
            return interest.subscriberCount.lt(subscriberCountCursor).or(interest.subscriberCount.eq(subscriberCountCursor).and(idCompareCondition));
          }
        case "createdAt":
          if (cursorValue == null) return null;
          Instant createdAtCursor = (Instant) cursorValue;
          if (isAscending) {
            return interest.createdAt.gt(createdAtCursor).or(interest.createdAt.eq(createdAtCursor).and(idCompareCondition));
          } else {
            return interest.createdAt.lt(createdAtCursor).or(interest.createdAt.eq(createdAtCursor).and(idCompareCondition));
          }
        case "id": // ID 정렬 시 (cursorValue는 Long 타입의 ID 값)
          if (isAscending) {
            return interest.id.gt(cursorId); // cursorId 자체가 cursorValue와 같음
          } else {
            return interest.id.lt(cursorId); // ID 내림차순
          }
        default:
          log.warn("Unsupported sort property for cursor pagination: '{}'. Defaulting to ID > cursor.", property);
          return interest.id.gt(cursorId); // 지원하지 않는 속성 시 기본 ID 오름차순
      }
    } catch (ClassCastException e) {
      log.error("Cursor value type mismatch for property '{}'. Expected type compatible with field. CursorValue: {}", property, cursorValue, e);
      // 타입 불일치 시 안전하게 ID 기준으로만 처리
      return interest.id.gt(cursorId);
    }
  }
  // --- ^^^ 커서 조건 생성 메서드 리팩토링 ^^^ ---


  /**
   * Pageable의 Sort 정보를 QueryDSL OrderSpecifier 목록으로 변환
   */
  private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orders = new ArrayList<>();
    if (sort != null && !sort.isEmpty()) {
      sort.forEach(order -> {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        String property = order.getProperty();
        ComparableExpressionBase<?> sortPath;
        switch (property) {
          case "name": sortPath = interest.name; break;
          case "subscriberCount": sortPath = interest.subscriberCount; break;
          case "createdAt": sortPath = interest.createdAt; break;
          case "id": sortPath = interest.id; break; // ID 정렬도 명시적으로 추가 가능
          default:
            log.warn("Unsupported sort property '{}'. Ignoring this sort order.", property);
            return; // 미지원 속성은 무시
        }
        orders.add(new OrderSpecifier(direction, sortPath));
      });
    }
    // 기본 정렬 및 커서 페이지네이션 안정성을 위해 ID 오름차순을 항상 마지막에 추가 (만약 명시적 ID 정렬이 없다면)
    if (orders.stream().noneMatch(o -> o.getTarget().equals(interest.id))) {
      orders.add(new OrderSpecifier<>(Order.ASC, interest.id));
    }
    return orders;
  }
}

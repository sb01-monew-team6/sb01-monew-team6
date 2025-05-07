package com.sprint.part3.sb01_monew_team6.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.sprint.part3.sb01_monew_team6.entity.QInterest.interest;

@RequiredArgsConstructor
@Slf4j
public class InterestRepositoryImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Interest> searchWithCursor(String keyword,
      Pageable pageable,
      Long cursorId,
      Object cursorValue) {
    List<Interest> results = queryFactory
        .selectFrom(interest)
        .where(
            keywordCondition(keyword),
            cursorCondition(cursorId, cursorValue, pageable.getSort())
        )
        .orderBy(toOrderSpecifiers(pageable.getSort()).toArray(new OrderSpecifier[0]))
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = results.size() > pageable.getPageSize();
    if (hasNext) results.remove(results.size() - 1);

    return new SliceImpl<>(results, pageable, hasNext);
  }

  private BooleanExpression keywordCondition(String keyword) {
    if (!StringUtils.hasText(keyword))
      return null;
    BooleanExpression byName = interest.name.containsIgnoreCase(keyword);
    BooleanExpression byKw = interest.keywords.containsIgnoreCase(keyword);
    return byName.or(byKw);
  }

  private BooleanExpression cursorCondition(Long cursorId,
      Object cursorValue,
      Sort sort) {
    if (cursorId == null) {
      // 첫 페이지
      return null;
    }

    // 정렬이 없거나 단일 ID 정렬인 경우
    if (sort == null
        || !sort.iterator().hasNext()
        || sort.stream().allMatch(o -> "id".equals(o.getProperty()))) {
      return interest.id.gt(cursorId);
    }

    // 첫 번째 정렬 기준만 사용
    Sort.Order order = sort.iterator().next();
    boolean asc = order.isAscending();
    String prop = order.getProperty();

    if (cursorValue == null) {
      return interest.id.gt(cursorId);
    }

    switch (prop) {
      case "name": {
        String nameCursor = (String) cursorValue;
        if (asc) {
          return interest.name.lower().gt(nameCursor.toLowerCase())
              .or(interest.name.lower().eq(nameCursor.toLowerCase())
                  .and(interest.id.gt(cursorId)));
        } else {
          return interest.name.lower().lt(nameCursor.toLowerCase())
              .or(interest.name.lower().eq(nameCursor.toLowerCase())
                  .and(interest.id.gt(cursorId)));
        }
      }

      case "subscriberCount": {
        Long cntCursor = ((Number) cursorValue).longValue();
        if (asc) {
          return interest.subscriberCount.gt(cntCursor)
              .or(interest.subscriberCount.eq(cntCursor)
                  .and(interest.id.gt(cursorId)));
        } else {
          return interest.subscriberCount.lt(cntCursor)
              .or(interest.subscriberCount.eq(cntCursor)
                  .and(interest.id.gt(cursorId)));
        }
      }

      case "createdAt": {
        Instant atCursor = (cursorValue instanceof Instant)
            ? (Instant) cursorValue
            : Instant.parse(cursorValue.toString());
        if (asc) {
          return interest.createdAt.gt(atCursor)
              .or(interest.createdAt.eq(atCursor)
                  .and(interest.id.gt(cursorId)));
        } else {
          return interest.createdAt.lt(atCursor)
              .or(interest.createdAt.eq(atCursor)
                  .and(interest.id.gt(cursorId)));
        }
      }

      default:
        // 지원하지 않는 정렬 속성 → ID 기준만
        return interest.id.gt(cursorId);
    }
  }

  /**
   * Sort → QueryDSL OrderSpecifier 변환 (한글 정렬을 위해 name은 lower() 사용)
   */
  private List<OrderSpecifier<?>> toOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orders = new ArrayList<>();
    boolean idExplicit = false;

    if (sort != null && !sort.isEmpty()) {
      for (Sort.Order order : sort) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        String prop = order.getProperty();

        switch (prop) {
          case "name":
            orders.add(new OrderSpecifier<>(direction, interest.name.lower()));
            break;
          case "subscriberCount":
            orders.add(new OrderSpecifier<>(direction, interest.subscriberCount));
            break;
          case "createdAt":
            orders.add(new OrderSpecifier<>(direction, interest.createdAt));
            break;
          case "id":
            orders.add(new OrderSpecifier<>(direction, interest.id));
            idExplicit = true;
            break;
          default:
            // 무시
        }
      }
    }

    // 명시적으로 id 정렬을 요청하지 않았다면, tie-breaker로 붙인다
    if (!idExplicit) {
      orders.add(new OrderSpecifier<>(Order.ASC, interest.id));
    }

    return orders;
  }
}
package com.sprint.part3.sb01_monew_team6.repository.news;

import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticle.newsArticle;
import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticleInterest.newsArticleInterest;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsArticleRepositoryImpl implements NewsArticleRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<NewsArticle> searchArticles(
      CursorPageRequestArticleDto request,
      OrderSpecifier<?> orderSpec,
      Long cursor,
      Instant after,
      int limit) {

    // 정렬 방향 ASC/DESC 판별
    boolean isDesc = request.direction().equalsIgnoreCase("DESC");

    // 발행일 기준(primary) 정렬 스펙 결정 - 컨트롤러에서 이미 넘겨준 orderSpec이 있으면 그것을 사용하고,
    //  없으면 publishDate로 기본 OrderSpecifier 생성
    OrderSpecifier<?> dateOrder = orderSpec != null
        ? orderSpec
        : new OrderSpecifier<>(
            isDesc ? Order.DESC : Order.ASC,
            newsArticle.articlePublishedDate
        );

    // ID 기준(secondary) 정렬 스펙 결정, 같은 publishDate 내에서 ID로 순서를 보장하기 위함
    OrderSpecifier<?> idOrder = isDesc
        ? newsArticle.id.desc()
        : newsArticle.id.asc();

    // 공통 WHERE 절: 삭제되지 않은 기사만 조회
    BooleanExpression where = newsArticle.isDeleted.eq(false);

    // 키워드 검색 조건 (제목 또는 요약에 포함)
    if (request.keyword() != null) {
      where = where.and(
          newsArticle.articleTitle.containsIgnoreCase(request.keyword())
              .or(newsArticle.articleSummary.containsIgnoreCase(request.keyword()))
      );
    }
    // 관심사(interestId) 필터 (조인 테이블 서브쿼리)
    if (request.interestId() != null) {
      where = where.and(
          newsArticle.id.in(
              jpaQueryFactory
                  .select(newsArticleInterest.newsArticle.id)
                  .from(newsArticleInterest)
                  .where(newsArticleInterest.interest.id.eq(request.interestId()))
          )
      );
    }
    // 출처(source) 필터
    if (request.sourceIn() != null && !request.sourceIn().isEmpty()) {
      where = where.and(newsArticle.source.in(request.sourceIn()));
    }
    // 발행일 from/to 기간 필터
    if (request.publishDateFrom() != null) {
      where = where.and(newsArticle.articlePublishedDate.goe(request.publishDateFrom()));
    }
    if (request.publishDateTo() != null) {
      where = where.and(newsArticle.articlePublishedDate.loe(request.publishDateTo()));
    }

    // 멀티컬럼 커서 조건 (publishDate + id)
    BooleanExpression cursorCond = null;
    if (after != null && cursor != null) {
      if (isDesc) {
        cursorCond = newsArticle.articlePublishedDate.lt(after)
            .or(
                newsArticle.articlePublishedDate.eq(after)
                    .and(newsArticle.id.lt(cursor))
            );
      } else {
        cursorCond = newsArticle.articlePublishedDate.gt(after)
            .or(
                newsArticle.articlePublishedDate.eq(after)
                    .and(newsArticle.id.gt(cursor))
            );
      }
    } else if (after != null) {
      // after만 있을 때
      cursorCond = isDesc
          ? newsArticle.articlePublishedDate.lt(after)
          : newsArticle.articlePublishedDate.gt(after);
    } else if (cursor != null) {
      // cursor(id)만 있을 때
      cursorCond = isDesc
          ? newsArticle.id.lt(cursor)
          : newsArticle.id.gt(cursor);
    }
    //커서 조건이 설정되었다면 WHERE 절에 추가
    if (cursorCond != null) {
      where = where.and(cursorCond);
    }

    // 최종 쿼리 실행: where + orderBy(발행일, ID) + limit
    return jpaQueryFactory
        .selectFrom(newsArticle)
        .where(where)
        .orderBy(dateOrder, idOrder)
        .limit(limit)
        .fetch();
  }



  //카운트 쿼리
  // cursor/after 는 전체 count 에 포함하지 않음
  @Override
  public long countArticles(CursorPageRequestArticleDto req) {
    //삭제되지 않은 기사만 포함
    BooleanExpression where = newsArticle.isDeleted.eq(false);

    // 키워드, 관심사, 출처, 기간 필터는 searchArticles와 동일하게 적용
    if (req.keyword() != null) {
      where = where.and(
          newsArticle.articleTitle.containsIgnoreCase(req.keyword())
              .or(newsArticle.articleSummary.containsIgnoreCase(req.keyword()))
      );
    }
    if (req.interestId() != null) {
      where = where.and(
          newsArticle.id.in(
              jpaQueryFactory.select(newsArticleInterest.newsArticle.id)
                  .from(newsArticleInterest)
                  .where(newsArticleInterest.interest.id.eq(req.interestId()))
          )
      );
    }
    if (req.sourceIn() != null && !req.sourceIn().isEmpty()) {
      where = where.and(newsArticle.source.in(req.sourceIn()));
    }
    if (req.publishDateFrom() != null) {
      where = where.and(newsArticle.articlePublishedDate.goe(req.publishDateFrom()));
    }
    if (req.publishDateTo() != null) {
      where = where.and(newsArticle.articlePublishedDate.loe(req.publishDateTo()));
    }

    // count 쿼리 실행
    Long totalWrapper = jpaQueryFactory
        .select(newsArticle.count())
        .from(newsArticle)
        .where(where)
        .fetchOne();

    long total = (totalWrapper != null ? totalWrapper : 0L);
    return total;
  }
}

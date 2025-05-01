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

  //검색(데이터 조회) 쿼리
  @Override
  public List<NewsArticle> searchArticles(CursorPageRequestArticleDto request,
      OrderSpecifier<?> orderSpec, Long cursor, Instant after, int limit) {
    //정렬 스펙 결정
    Order dir = request.direction().equalsIgnoreCase("ASC") ? Order.ASC : Order.DESC; // 요청된 방향에 따라 정렬 순서 결정

    OrderSpecifier<?> realOrder;
    if (orderSpec != null) {
      realOrder = orderSpec; //외부 정렬 스펙이 있으면 우선 적용
    } else {
      if ("publishDate".equals(request.orderBy())) {
        // 발행일 기준 정렬
        realOrder = new OrderSpecifier<Instant>(
            dir,
            newsArticle.articlePublishedDate
        );
      } else if ("title".equals(request.orderBy())) {
        // 제목 기준 정렬
        realOrder = new OrderSpecifier<String>(
            dir,
            newsArticle.articleTitle
        );
      } else {
        // 그 외 (기본) ID 기준 정렬
        realOrder = new OrderSpecifier<Long>(
            dir,
            newsArticle.id
        );
      }
    }
    //동적 WHERE 절(조건은 삭제되지 않은)
    //BooleanExpression : .and() 또는 .or()를 통해 누적
    BooleanExpression where = newsArticle.isDeleted.eq(false);

    // 제목 또는 요약에 키워드 포함 여부
    if (request.keyword() != null) {
      where = where.and(
          newsArticle.articleTitle.containsIgnoreCase(request.keyword())
              .or(newsArticle.articleSummary.containsIgnoreCase(request.keyword()))
      );
    }

    //조인 테이블에서 해당 interestId를 가진 articleId만 조회
    if (request.interestId() != null) {
      // QNewsArticleInterest 로 조인 테이블 검색
      where = where.and(
          newsArticle.id.in(
              jpaQueryFactory
                  .select(newsArticleInterest.newsArticle.id)
                  .from(newsArticleInterest)
                  .where(newsArticleInterest.interest.id.eq(request.interestId()))
          )
      );
    }

    //허용된 출처 목록에 속하는지 검사
    if (request.sourceIn() != null && !request.sourceIn().isEmpty()) {
      where = where.and(newsArticle.source.in(request.sourceIn()));
    }
    // from/to 기간 내의 기사
    if (request.publishDateFrom() != null) {
      where = where.and(newsArticle.articlePublishedDate.goe(request.publishDateFrom()));
    }
    if (request.publishDateTo() != null) {
      where = where.and(newsArticle.articlePublishedDate.loe(request.publishDateTo()));
    }
    // 커서 기반 페이징: ID가 cursor 값보다 작은 데이터만
    if (cursor != null) {
      where = where.and(newsArticle.id.lt(cursor));
    }
    // 특정 시간 이전(createdAt < after) 필터
    if (after != null) {
      where = where.and(newsArticle.createdAt.goe(after));
    }


    // 쿼리 실행
    return jpaQueryFactory.selectFrom(newsArticle)
        .where(where)
        .orderBy(realOrder)
        .limit(limit)
        .fetch();
  }

  //카운트 쿼리
  // cursor/after 는 전체 count 에 포함하지 않음
  @Override
  public long countArticles(CursorPageRequestArticleDto req) {
    BooleanExpression where = newsArticle.isDeleted.eq(false);

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

    Long totalWrapper = jpaQueryFactory
        .select(newsArticle.count())
        .from(newsArticle)
        .where(where)
        .fetchOne();

    long total = (totalWrapper != null ? totalWrapper : 0L);
    return total;
  }
}

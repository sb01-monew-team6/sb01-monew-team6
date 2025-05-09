package com.sprint.part3.sb01_monew_team6.repository.news;

import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticle.newsArticle;
import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticleInterest.newsArticleInterest;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.QComment;
import com.sprint.part3.sb01_monew_team6.entity.QNewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.QNewsArticleInterest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    QNewsArticle article = QNewsArticle.newsArticle;
    QComment comment = QComment.comment;

    // 정렬 방향 ASC/DESC 판별
    boolean isDesc = request.direction().equalsIgnoreCase("DESC");

    // 댓글 수 기준 정렬
    if ("commentCount".equalsIgnoreCase(request.orderBy())) {
      // 삭제되지 않은 댓글만 LEFT JOIN
      JPAQuery<NewsArticle> query = jpaQueryFactory
          .select(article)
          .from(article)
          .leftJoin(article.comments, comment)
          .on(comment.isDeleted.eq(false))
          .where(buildWhere(request, article))
          .groupBy(article.id);

      // 커서 페이징
      BooleanExpression cursorCond = buildCursorCond(request, article);
      if (cursorCond != null) {
        query.where(cursorCond);
      }

      // 정렬: 댓글 수 내림차순(or 오름차순), 동률인 경우 발행일 내림차순, 그다음 ID 순
      query.orderBy(
          new OrderSpecifier<>(isDesc ? Order.DESC : Order.ASC, comment.id.count()),
          new OrderSpecifier<>(Order.DESC, article.articlePublishedDate),
          isDesc ? article.id.desc() : article.id.asc()
      );

      return query.limit(limit).fetch();
    }

    // 그 외 (publishDate, viewCount 등) 기존 로직
    JPAQuery<NewsArticle> query2 = jpaQueryFactory
        .selectFrom(article)
        .where(
            buildWhere(request, article),
            buildCursorCond(request, article)
        )
        .orderBy(
            orderSpec,
            isDesc ? article.id.desc() : article.id.asc()
        )
        .limit(limit);

    return query2.fetch();
  }

  @Override
  public long countArticles(CursorPageRequestArticleDto request) {
    QNewsArticle article = QNewsArticle.newsArticle;
    BooleanExpression where = buildWhere(request, article);

    Long total = jpaQueryFactory
        .select(article.count())
        .from(article)
        .where(where)
        .fetchOne();

    return total != null ? total : 0L;
  }

  // 검색 조건 생성(helper)
  private BooleanExpression buildWhere(CursorPageRequestArticleDto req, QNewsArticle a) {
    BooleanExpression where = a.isDeleted.eq(false);

    if (req.keyword() != null) {
      where = where.and(
          a.articleTitle.containsIgnoreCase(req.keyword())
              .or(a.articleSummary.containsIgnoreCase(req.keyword()))
      );
    }
    if (req.interestId() != null) {
      where = where.and(a.id.in(
          jpaQueryFactory
              .select(QNewsArticleInterest.newsArticleInterest.newsArticle.id)
              .from(QNewsArticleInterest.newsArticleInterest)
              .where(QNewsArticleInterest.newsArticleInterest.interest.id.eq(req.interestId()))
      ));
    }
    if (req.sourceIn() != null && !req.sourceIn().isEmpty()) {
      where = where.and(a.source.in(req.sourceIn()));
    }
    if (req.publishDateFrom() != null) {
      where = where.and(a.articlePublishedDate.goe(req.publishDateFrom()));
    }
    if (req.publishDateTo() != null) {
      where = where.and(a.articlePublishedDate.loe(req.publishDateTo()));
    }
    return where;
  }

  // 커서(다중 컬럼) 페이징 조건 생성(helper)
  private BooleanExpression buildCursorCond(CursorPageRequestArticleDto req, QNewsArticle a) {
    boolean isDesc = req.direction().equalsIgnoreCase("DESC");
    Long    cur    = Optional.ofNullable(req.cursor())
        .filter(s -> !s.isBlank())
        .map(Long::valueOf)
        .orElse(null);
    Instant aft    = req.after();

    if (cur == null && aft == null) {
      return null;
    }
    if (cur != null && aft != null) {
      return isDesc
          ? a.articlePublishedDate.lt(aft)
          .or(a.articlePublishedDate.eq(aft).and(a.id.lt(cur)))
          : a.articlePublishedDate.gt(aft)
              .or(a.articlePublishedDate.eq(aft).and(a.id.gt(cur)));
    }
    if (aft != null) {
      return isDesc
          ? a.articlePublishedDate.lt(aft)
          : a.articlePublishedDate.gt(aft);
    }
    // cur != null 만 있는 경우
    return isDesc
        ? a.id.lt(cur)
        : a.id.gt(cur);
  }
}

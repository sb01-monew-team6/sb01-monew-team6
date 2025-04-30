package com.sprint.part3.sb01_monew_team6.service.news.impl;

import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticle.newsArticle;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageResponseArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.ArticleService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
  private final NewsArticleRepository newsArticleRepository;
  private final CommentRepository commentRepository;

  @Transactional(readOnly=true)
  @Override
  public CursorPageResponseArticleDto searchArticles(CursorPageRequestArticleDto request) {
    // cursor : null 또는 빈 문자열이면 null 그대로
    Long cursor = Optional.ofNullable(request.cursor())
        .filter(s -> !s.isBlank())
        .map(Long::valueOf)
        .orElse(null);

    // after : 이미 Instant 타입
    Instant after = request.after();

    // 정렬 스펙 생성
    OrderSpecifier<?> orderSpec = buildOrder(request);

    // 실제 페이징 조회
    List<NewsArticle> newsArticles = newsArticleRepository.searchArticles(
        request, orderSpec, cursor, after, request.limit()
    );
    long totalElements = newsArticleRepository.countArticles(request);

    // DTO 변환
    List<ArticleDto> content = newsArticles.stream()
        .map(a -> ArticleDto.from(
            a,
            commentRepository.countByArticleId(a.getId()),
            0L,
            false
        ))
        .toList();

    // 페이징 정보 계산
    boolean hasNext = content.size() == request.limit();
    String nextCursor = hasNext
        ? String.valueOf(content.get(content.size() - 1).id())
        : null;
    Instant nextAfter = hasNext
        ? content.get(content.size() - 1).publishDate()
        : null;

    // 응답 생성
    return CursorPageResponseArticleDto.toDto(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  private OrderSpecifier<?> buildOrder(CursorPageRequestArticleDto request) {
    Order dir = request.direction().equalsIgnoreCase("ASC") ? Order.ASC : Order.DESC;
    if ("publishDate".equals(request.orderBy())) {
      return new OrderSpecifier<>(dir, newsArticle.articlePublishedDate);
    } else if ("title".equals(request.orderBy())) {
      return new OrderSpecifier<>(dir, newsArticle.articleTitle);
    } else {
      return new OrderSpecifier<>(dir, newsArticle.id);
    }
  }
}

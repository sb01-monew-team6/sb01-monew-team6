package com.sprint.part3.sb01_monew_team6.service.news.impl;

import static com.sprint.part3.sb01_monew_team6.entity.QNewsArticle.newsArticle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleDto;
import com.sprint.part3.sb01_monew_team6.dto.news.ArticleRestoreResultDto;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.ArticleService;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {
  private final NewsArticleRepository newsArticleRepository;
  private final CommentRepository commentRepository;
  private final PageResponseMapper pageResponseMapper;
  private final S3Client s3Client;
//  @Value("${cloud.aws.s3.bucket}")
  private final String bucketName = "monew";
  private final ObjectMapper objectMapper;

  //목록 조회 : 페이지네이션
  @Transactional(readOnly=true)
  @Override
  public PageResponse<ArticleDto> searchArticles(CursorPageRequestArticleDto request) {
    log.debug("기사조회(searchArticles) 시작 → userId={}, keyword={}, orderBy={}, direction={}, cursor={}, after={}, limit={}",
        request.userId(), request.keyword(), request.orderBy(), request.direction(), request.cursor(), request.after(), request.limit()
    );

    // limit 음수 방지
    if (request.limit() <= 0) {
      log.warn("잘못된 limit: {}", request.limit());
      throw new NewsException(ErrorCode.NEWS_LIMIT_MORE_THAN_ONE_EXCEPTION,Instant.now(), HttpStatus.BAD_REQUEST);
    }

    // 정렬 스펙 생성
    OrderSpecifier<?> orderSpec;
    try {
      orderSpec = buildOrder(request);
    } catch (IllegalArgumentException ex) {
      log.error("지원하지 않는 orderBy: {}", request.orderBy(), ex);
      throw new NewsException(ErrorCode.NEWS_ORDERBY_IS_NOT_SUPPORT_EXCEPTION,Instant.now(),HttpStatus.BAD_REQUEST);
    }

    // cursor : null 또는 빈 문자열이면 null 그대로
    Long cursor = Optional.ofNullable(request.cursor())
        .filter(s -> !s.isBlank())
        .map(Long::valueOf)
        .orElse(null);

    // after : 이미 Instant 타입
    Instant after = request.after();

    List<NewsArticle> entities;
    long total;
    try {
      entities = newsArticleRepository.searchArticles(request, orderSpec, cursor, after, request.limit());
      total = newsArticleRepository.countArticles(request);
    } catch (Exception ex) {
      log.error("NewsArticleRepository 호출 중 예외", ex);
      throw new NewsException(ErrorCode.NEWS_CALL_NEWSARTICLEREPOSITORY_EXCEPTION,Instant.now(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Entity → DTO 변환
    List<ArticleDto> dtos = entities.stream()
        .map(a -> ArticleDto.from(
            a,
            commentRepository.countByArticleId(a.getId()),
            0L,
            false
        ))
        .collect(Collectors.toList());

    // Slice 생성
    boolean hasNext = dtos.size() == request.limit();
    PageRequest pgReq = PageRequest.of(0, request.limit());
    Slice<ArticleDto> slice = new SliceImpl<>(dtos, pgReq, hasNext);

    // nextCursor / nextAfter 결정
    Object nextCursor = hasNext ? dtos.get(dtos.size() - 1).id() : null;
    Object nextAfter  = hasNext ? dtos.get(dtos.size() - 1).publishDate() : null;


    // 응답 생성
    PageResponse<ArticleDto> response = pageResponseMapper.fromSlice(slice, nextCursor, nextAfter, total);
    log.debug("기사조회(searchArticles) 종료 → returned {} items, total={}", response.contents().size(), response.totalElements());
    return response;
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
  //백업 복구
  @Override
  public List<ArticleRestoreResultDto> restore(LocalDate from, LocalDate to) throws IOException {
    // 복구 결과를 날짜별로 담을 리스트 생성
    List<ArticleRestoreResultDto> restoreArticles = new ArrayList<>();

    // from부터 to까지 하루씩 순회하며 백업 파일 처리
    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
      // 2.1) S3에서 날짜별 백업 JSON 가져오기
      String key = String.format("backup/%s.json", date);
      GetObjectRequest getReq = GetObjectRequest.builder()
          .bucket(bucketName)    // S3 버킷 이름
          .key(key)              // 객체 키 (backup/2025-05-02.json 등)
          .build();
      ResponseBytes<GetObjectResponse> resp = s3Client.getObjectAsBytes(getReq);

      // JSON을 NewsArticle 리스트로 역직렬화
      List<NewsArticle> backups = objectMapper.readValue(
          resp.asByteArray(), new TypeReference<List<NewsArticle>>() {}
      );

      // 이 날짜에 새로 복구된 기사 ID를 모을 리스트
      List<Long> restoredIds = new ArrayList<>();

      // 백업된 각 기사에 대해 DB 존재 여부 확인 후 복구
      for (NewsArticle backup : backups) {
        if (!newsArticleRepository.existsBySourceUrl(backup.getSourceUrl())) {
          NewsArticle toSave = NewsArticle.builder()
              .source(backup.getSource())
              .sourceUrl(backup.getSourceUrl())
              .articleTitle(backup.getArticleTitle())
              .articlePublishedDate(backup.getArticlePublishedDate())
              .articleSummary(backup.getArticleSummary())
              .isDeleted(false)    // 논리삭제 초기화
              .build();

          // 복구 엔티티 저장 및 ID 수집
          NewsArticle saved = newsArticleRepository.save(toSave);
          restoredIds.add(saved.getId());
        }
      }

      // 날짜 복구 결과 DTO 생성 및 리스트에 추가
      restoreArticles.add(new ArticleRestoreResultDto(
          date,
          restoredIds,
          restoredIds.size()
      ));
    }

    return restoreArticles;
  }
}

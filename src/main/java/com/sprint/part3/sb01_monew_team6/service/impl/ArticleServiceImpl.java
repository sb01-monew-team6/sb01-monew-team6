package com.sprint.part3.sb01_monew_team6.service.impl;

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
import com.sprint.part3.sb01_monew_team6.service.ArticleService;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {
  private final NewsArticleRepository newsArticleRepository;
  private final CommentRepository commentRepository;
  private final PageResponseMapper pageResponseMapper;
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  @Value("${storage.s3.backup-bucket}")
  private String bucketName;

  /** 테스트 전용으로만 여는 세터 */
  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

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
      throw new NewsException(ErrorCode.NEWS_LIMIT_MORE_THAN_ONE_EXCEPTION, Instant.now(), HttpStatus.BAD_REQUEST);
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
            commentRepository.countByArticleIdAndIsDeletedFalse(a.getId()),
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

  private OrderSpecifier<?> buildOrder(CursorPageRequestArticleDto req) {
    Order dir = "ASC".equalsIgnoreCase(req.direction()) ? Order.ASC : Order.DESC;
    switch (req.orderBy()) {
      case "publishDate":
        return new OrderSpecifier<>(dir, newsArticle.articlePublishedDate);
      case "viewCount":
        return new OrderSpecifier<>(dir, newsArticle.articleViews.size());
      case "commentCount":
        return new OrderSpecifier<>(dir, newsArticle.comments.size());
      default:
        throw new NewsException(ErrorCode.NEWS_ORDERBY_IS_NOT_SUPPORT_EXCEPTION,Instant.now(),HttpStatus.BAD_REQUEST);
    }
  }

  //복구
  @Override
  public List<ArticleRestoreResultDto> restore(LocalDate from, LocalDate to) throws IOException {
    log.info("백업 복구 시작 : from={}, to={}", from, to);
    // 복구 결과를 날짜별로 담을 리스트 생성
    List<ArticleRestoreResultDto> restoreArticles = new ArrayList<>();

    // from부터 to까지 하루씩 순회하며 백업 파일 처리
    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
      // S3에서 날짜별 백업 JSON 가져오기
      String key = String.format("backup/%s.json", date);
      log.info("날짜별 복구 처리 : date={}, key={}", date, key);

      List<Long> restoredIds = new ArrayList<>();
      try{
        //S3에서 가져오기
        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(bucketName)    // S3 버킷 이름
            .key(key)              // 객체 키 (backup/2025-05-02.json 등)
            .build();
        ResponseBytes<GetObjectResponse> resp = s3Client.getObjectAsBytes(getReq);

        // JSON을 NewsArticle 리스트로 역직렬화
        List<NewsArticle> backups = objectMapper.readValue(
            resp.asByteArray(), new TypeReference<List<NewsArticle>>() {}
        );

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
            log.debug("복구 완료 : sourceUrl={}, id={}", backup.getSourceUrl(), saved.getId());
          }else{
            log.debug("이미 존재하여 넘어감 : sourceUrl={}, id={}", backup.getSourceUrl(), backup.getId());
          }
        }
      }catch(NoSuchElementException e){
        //S3 에 파일이 없는 경우
        log.warn("백업 파일 없어서 건너뜀 : key={}(date={})", key, date);
      }catch (IOException | S3Exception e){
        log.error("백업 복구 중 오류 : date={}, key={}, error={}", date, key, e.getMessage());
        throw new NewsException(ErrorCode.INTERNAL_SERVER_ERROR,Instant.now(),HttpStatus.INTERNAL_SERVER_ERROR);
      }

      // 날짜 복구 결과 DTO 생성 및 리스트에 추가
      restoreArticles.add(new ArticleRestoreResultDto(
          date,
          restoredIds,
          restoredIds.size()
      ));
      log.info("날짜별 복구 결과 : date={} , restoredCount={}", date, restoredIds.size());
    }
    log.info("백업 복구 완료: totalDays={} 결과건수={}", ChronoUnit.DAYS.between(from, to) + 1, restoreArticles.size());
    return restoreArticles;
  }

  //백업
  @Transactional(readOnly = true)
  public void backup(LocalDate date) {
    log.info("백업 시작: date={}", date);
    try {
      Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
      Instant end   = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

      // 이 시점부터 세션이 열려 있어, Jackson 직렬화 시 lazy 필드도 초기화 가능
      List<NewsArticle> list = newsArticleRepository.findAllByCreatedAtBetween(start, end);
      log.debug("조회된 기사 수: {}", list.size());

      String json = objectMapper.writeValueAsString(list);
      String key  = String.format("backup/%s.json", date);
      log.debug("S3 업로드 키: {}", key);

      PutObjectRequest req = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();
      s3Client.putObject(req, RequestBody.fromString(json));

      log.info("백업 완료: bucket={}, key={}, count={}", bucketName, key, list.size());
    } catch (IOException e) {
      log.error("JSON 직렬화 오류: date={}, error={}", date, e.getMessage(), e);
      throw new NewsException(ErrorCode.NESW_BACKUP_SERIALIZATION_FAILED_EXCEPTION, Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (S3Exception e) {
      log.error("S3 업로드 실패: bucket={}, date={}, error={}", bucketName, date, e.getMessage(), e);
      throw new NewsException(ErrorCode.NEWS_BACKUP_S3_UPLOAD_FAILED_EXCEPTION, Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error("알 수 없는 백업 오류: date={}, error={}", date, e.getMessage(), e);
      throw new NewsException(ErrorCode.INTERNAL_SERVER_ERROR, Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  //논리삭제
  @Override
  public void deleteArticle(Long articleId){
    log.info("논리 삭제 시작 : articleId={}", articleId);
    //기사가 있는지 id로 확인
    NewsArticle newsArticle = newsArticleRepository.findById(articleId)
        .orElseThrow(() ->{
          log.error("논리 삭제 실패(기사 미발견) : articleId={}", articleId);
          return new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND);
        });
    //isDeleted = true
    newsArticle.changeDeleted();

    newsArticleRepository.save(newsArticle);
    log.info("논리 삭제 완료 : articleId={}", articleId);
  }

  //물리삭제
  @Override
  public void hardDeleteArticle(Long articleId){
    log.info("물리 삭제 시작 : articleId={}", articleId);
    //기사가 있는지 id로 확인
    if(!newsArticleRepository.existsById(articleId)){
      log.error("물리 삭제 실패(기사 미발견) : articleId={}", articleId);
      throw new NewsException(ErrorCode.NEWS_ARTICLE_NOT_FOUND_EXCEPTION, Instant.now(), HttpStatus.NOT_FOUND);
    }

    //hard 삭제
    newsArticleRepository.deleteById(articleId);
    log.info("물리 삭제 완료 : articleId={}", articleId);
  }
}

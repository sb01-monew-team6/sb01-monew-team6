package com.sprint.part3.sb01_monew_team6.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
public class ArticleBackupTasklet implements Tasklet {
  private final NewsArticleRepository newsArticleRepository;
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  @Value("${storage.s3.backup-bucket}")
  private String bucketName;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    //00:00 ~ 익일 00:00 UTC 범위 계산
    LocalDate today = LocalDate.now();
    Instant start = today.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant end = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    // 해당 기간 기사 조회
    List<NewsArticle> articles = newsArticleRepository.findAllByCreatedAtBetween(start, end);

    // JSON 직렬화
    String json = objectMapper.writeValueAsString(articles);

    // S3에 업로드
    String key = String.format("backup/%s.json", today);
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();
    s3Client.putObject(request, RequestBody.fromString(json));

    return RepeatStatus.FINISHED;
  }
}

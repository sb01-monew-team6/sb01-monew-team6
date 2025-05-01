package com.sprint.part3.sb01_monew_team6.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
public class ArticleBackupTasklet implements Tasklet {
  private final NewsArticleRepository newsArticleRepository;
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  private final String bucketName;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    List<NewsArticle> articles = newsArticleRepository.findAllByCreatedAtBetween(null,null);
    String json = objectMapper.writeValueAsString(articles);

    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucketName).key("stub.json").build(),
        RequestBody.fromString(json)
    );
    return RepeatStatus.FINISHED;
  }
}

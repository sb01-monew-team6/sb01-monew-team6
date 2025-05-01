package com.sprint.part3.sb01_monew_team6.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class ArticleBackupTasklet implements Tasklet {
  private final NewsArticleRepository newsArticleRepository;
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  private final String bucketName;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    return null;
  }
}

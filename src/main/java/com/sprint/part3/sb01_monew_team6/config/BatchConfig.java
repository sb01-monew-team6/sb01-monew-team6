package com.sprint.part3.sb01_monew_team6.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import com.sprint.part3.sb01_monew_team6.service.impl.NewsCollectionServiceImpl;
import com.sprint.part3.sb01_monew_team6.storage.s3.ArticleBackupTasklet;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.NonSkippableReadException;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {
  private final NewsCollectionServiceImpl service;

  //newsJob이라는 이름의 배치 잡을 생성
  @Bean @Qualifier(value = "newsJob")
  public Job newsJob(JobRepository jobRepository, Step newsStep) {
    return new JobBuilder("newsJob",jobRepository)
        .incrementer(new RunIdIncrementer()) // 매 실행마다 ID를 새로 생성
        .start(newsStep)                     // newsStep 스텝을 잡의 첫 단계로 등록
        .build();
  }

  // 서비스에서 뉴스 목록을 읽어 ListItemReader로 래핑
  @Bean
  @StepScope // 실제 배치 Step이 실행될 때 호출됩니다.
  public ItemReader<ExternalNewsItem> newsReader(NewsCollectionServiceImpl service) {
    List<ExternalNewsItem> allNews;
    try {
      allNews = service.fetchCandidates();
    } catch (Exception e) {
      log.warn("Batch reader에서 예외 발생, 빈 리스트로 대체: {}", e.getMessage());
      allNews = List.of();
    }
    return new ListItemReader<>(allNews);
  }

  // ExternalNewsItem → NewsArticle 변환
  // ItemProcessor<I, O>
  @Bean
  public ItemProcessor<ExternalNewsItem, NewsArticle> newsProcessor() {
    return NewsArticle::from; // return ExternalNewsItem->NewsArticle.from(ExternalNewsItem);
  }

  //변환된 NewsArticle 목록을 서비스에 저장 위임
  @Bean
  public ItemWriter<NewsArticle> newsWriter() {
    return (Chunk<? extends NewsArticle> chunk) -> { //스프링 배치의 내부 자료구조(Chunk<T>
      // Chunk 안에 담긴 List<NewsArticle>를 꺼내서 service에 넘김
      List<NewsArticle> articles = new ArrayList<>(chunk.getItems());
      service.saveAll(articles);
    };
  }

  @Bean
  public Step newsStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ItemReader<ExternalNewsItem> reader,
      ItemProcessor<ExternalNewsItem,NewsArticle> processor,
      ItemWriter<NewsArticle> writer) {
    return new StepBuilder("newsStep", jobRepository)
        .<ExternalNewsItem, NewsArticle>chunk(10, transactionManager) //트랜잭션 단위이자 커밋 단위가 10건
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()                                 // fault-tolerant 모드 활성화
        .skipLimit(100)                                // 최대 100개 예외 스킵
        .skip(NonSkippableReadException.class)         // 읽기 단계에서 래핑되어 올라오는 예외
        .skip(Exception.class)                         // 모든 Exception 스킵
        .retryLimit(3)                                 // 최대 3회 재시도
        .retry(IOException.class)                      // IOException 에 한해 재시도
        .build();
  }

  /**
   * Tasklet 빈 정의
   */
  @Bean
  public Tasklet articleBackupTasklet(
      NewsArticleRepository repo,
      S3Client s3Client,
      ObjectMapper objectMapper
  ) {
    return new ArticleBackupTasklet(repo, s3Client, objectMapper);
  }

  @Bean
  public Step backupStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      Tasklet articleBackupTasklet
  ) {
    return new StepBuilder("backupStep", jobRepository)
        .tasklet(articleBackupTasklet, transactionManager)  // ← 신규 오버로드
        .build();
  }

  @Bean @Qualifier(value = "backupJob")
  public Job backupJob(JobRepository jobRepository, Step backupStep) {
    return new JobBuilder("backupJob", jobRepository)
        .start(backupStep)
        .build();
  }
}

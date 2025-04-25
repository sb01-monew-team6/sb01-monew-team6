package com.sprint.part3.sb01_monew_team6.config;

import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
  private final NewsCollectionService service;

  @Bean @Qualifier(value = "newsJob")
  public Job newsJob(JobRepository jobRepository, Step newsStep) {
    return new JobBuilder("newsJob",jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(newsStep)
        .build();
  }
  // 서비스에서 뉴스 목록을 읽어 ListItemReader로 래핑
  @Bean
  public ItemReader<ExternalNewsItem> newsReader(NewsCollectionService service) {
    List<ExternalNewsItem> allNews = service.fetchCandidates();
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
    return (Chunk<? extends NewsArticle> chunk) -> {
      // Chunk 안에 담긴 List<NewsArticle>를 꺼내서 service에 넘깁니다.
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
    return new StepBuilder("newsStep",jobRepository)
        .<ExternalNewsItem,NewsArticle>chunk(10,transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();

  }
}

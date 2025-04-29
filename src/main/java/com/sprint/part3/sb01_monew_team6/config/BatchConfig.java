package com.sprint.part3.sb01_monew_team6.config;

import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
        .skip(Exception.class)                         // 모든 Exception 스킵
        .retryLimit(3)                                 // 최대 3회 재시도
        .retry(IOException.class)                      // IOException 에 한해 재시도
        .build();
  }
}

package com.sprint.part3.sb01_monew_team6.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.service.news.impl.NewsCollectionImplService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
public class BatchConfigTest {
  @Mock
  private NewsCollectionImplService service;
  @Mock
  private JobRepository jobRepository;
  @Mock
  private Step newsStep;
  @Mock
  private PlatformTransactionManager transactionManager;

  @InjectMocks
  private BatchConfig config;

  private ExternalNewsItem item1;
  private ExternalNewsItem item2;

  @BeforeEach
  void setUp() {
    item1 = new ExternalNewsItem("Naver", "url1", "url1", "title1", Instant.now(), "desc1");
    item2 = new ExternalNewsItem("Rss", "url2", "url2", "title2", Instant.now(), "desc2");
  }

  @Test
  @DisplayName("newsReader : 서비스에서 받아온 목록을 순서대로 읽어옴")
  void newsReader_fromService() throws Exception {
    //given
    given(service.fetchCandidates()).willReturn(Arrays.asList(item1,item2));

    //when
    ItemReader<ExternalNewsItem> reader = config.newsReader(service);

    //then
    assertThat(reader.read()).isEqualTo(item1);
    assertThat(reader.read()).isEqualTo(item2);
    assertThat(reader.read()).isNull();
    then(service).should().fetchCandidates();
  }

  @Test
  @DisplayName("newsProcessor : ExternalNewsItem을 NewsArticle로 변환")
  void newsProcessor_transfer_externalNewsItem_to_newsArticle() throws Exception {
    //given
    ExternalNewsItem ex = new ExternalNewsItem("Naver","url1","url1","title1", Instant.now(),"desc1");
    //when
    ItemProcessor<ExternalNewsItem, NewsArticle> processor = config.newsProcessor();
    NewsArticle article = processor.process(ex);
    //then
    assertThat(article).isNotNull();
    assertThat(article.getSource()).isEqualTo(ex.provider());
    assertThat(article.getSourceUrl()).isEqualTo(ex.originalLink());
    assertThat(article.getArticleTitle()).isEqualTo(ex.title());
    assertThat(article.getArticlePublishedDate()).isEqualTo(ex.pubDate());
    assertThat(article.getArticleSummary()).isEqualTo(ex.description());
  }

  @Test
  @DisplayName("newsWriter : 변환된 기사 리스트를 서비스에 위임")
  void newsWriter_transferArticle_to_service() throws Exception {
    //given
    NewsArticle a1 = NewsArticle.from(item1);
    NewsArticle a2 = NewsArticle.from(item2);
    List<NewsArticle> articles = Arrays.asList(a1,a2);
    ItemWriter<NewsArticle> writer = config.newsWriter();
    //when
    Chunk<NewsArticle> chunk = new Chunk<>(articles);
    writer.write(chunk);
    //then
    then(service).should().saveAll(articles);
  }

  @Test
  @DisplayName("newsJob: 빈으로 Job이 생성")
  void newsJob_create_newsJob_Bean() {
    // when
    Job job = config.newsJob(jobRepository, newsStep);
    // then
    assertThat(job.getName()).isEqualTo("newsJob");
  }

  @Test
  @DisplayName("newsStep : Step 빈 생성")
  void newsStep_create_newsStep_Bean(){
    // given
    @SuppressWarnings("unchecked")
    ItemReader<ExternalNewsItem> readerMock = mock(ItemReader.class);
    @SuppressWarnings("unchecked")
    ItemProcessor<ExternalNewsItem, NewsArticle> processorMock = mock(ItemProcessor.class);
    @SuppressWarnings("unchecked")
    ItemWriter<NewsArticle> writerMock = mock(ItemWriter.class);
    // when
    Step step = config.newsStep(
        jobRepository,
        transactionManager,
        readerMock,
        processorMock,
        writerMock
    );
    // then
    assertThat(step.getName()).isEqualTo("newsStep");
  }
}

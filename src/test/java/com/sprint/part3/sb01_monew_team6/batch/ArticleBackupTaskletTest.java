package com.sprint.part3.sb01_monew_team6.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
public class ArticleBackupTaskletTest {
  @Mock
  NewsArticleRepository newsArticleRepository;
  @Mock
  S3Client s3Client;
  @Mock
  ObjectMapper objectMapper;
  @InjectMocks
  ArticleBackupTasklet tasklet;

  @Test
  @DisplayName("일별 백업 시 S3에 업로드 후 FINISHED 반환")
  void execute_uploadArticlesToS3_returnFinished() throws Exception {
    //given
    when(newsArticleRepository.findAllByCreatedAtBetween(any(),any())).thenReturn(List.of());
    when(objectMapper.writeValueAsString(any())).thenReturn("[]");

    //when
    RepeatStatus status = tasklet.execute(null,null);

    //then
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
  }
}

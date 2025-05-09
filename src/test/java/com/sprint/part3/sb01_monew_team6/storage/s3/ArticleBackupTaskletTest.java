package com.sprint.part3.sb01_monew_team6.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.storage.s3.ArticleBackupTasklet;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class ArticleBackupTaskletTest {

  @Mock
  private NewsArticleRepository newsArticleRepository;

  @Mock
  private S3Client s3Client;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private ArticleBackupTasklet tasklet;

  @BeforeEach
  void setUp() {
    // @Value로 주입되는 bucketName 필드 설정
    ReflectionTestUtils.setField(tasklet, "bucketName", "test-backup-bucket");
  }

  @Test
  @DisplayName("execute(): 정상적으로 S3 업로드 후 FINISHED 반환")
  void execute_success() throws Exception {
    // given
    List<NewsArticle> articles = List.of(new NewsArticle(), new NewsArticle());
    when(newsArticleRepository.findAllByCreatedAtBetween(any(), any()))
        .thenReturn(articles);

    String json = "[{\"dummy\":1}, {\"dummy\":2}]";
    when(objectMapper.writeValueAsString(articles))
        .thenReturn(json);

    // when
    RepeatStatus status = tasklet.execute(null, null);

    // then
    assertEquals(RepeatStatus.FINISHED, status);

    // S3Client.putObject 호출 확인
    ArgumentCaptor<PutObjectRequest> reqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCap = ArgumentCaptor.forClass(RequestBody.class);
    verify(s3Client, times(1)).putObject(reqCap.capture(), bodyCap.capture());

    PutObjectRequest req = reqCap.getValue();
    // bucket 이름 및 키 확인
    assertEquals("test-backup-bucket", req.bucket());
    String expectedKey = String.format("backup/%s.json", LocalDate.now());
    assertEquals(expectedKey, req.key());

    // RequestBody에 JSON이 포함되었는지 대략 검증
    RequestBody body = bodyCap.getValue();
    assertEquals(json.getBytes().length, body.contentLength());
  }

  @Test
  @DisplayName("execute(): S3 업로드 실패 시 예외를 그대로 던진다")
  void execute_s3FailureThrows() throws Exception {
    // given
    when(newsArticleRepository.findAllByCreatedAtBetween(any(), any()))
        .thenReturn(List.of());
    when(objectMapper.writeValueAsString(anyList()))
        .thenReturn("[]");

    doThrow(new RuntimeException("S3 error"))
        .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    // when / then
    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
      tasklet.execute(null, null);
    });
    assertEquals("S3 error", ex.getMessage());
  }
}

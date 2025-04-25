package com.sprint.part3.sb01_monew_team6.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
public class BatchConfigTest {
  @Mock
  private JobLauncher jobLauncher;
  @Mock
  private Job newsJob;
  @InjectMocks
  private BatchConfig config;

  @Test
  @DisplayName("runJon 호출 시 jobLauncher.run 호출")
  void whenRunJob_thenRunJobLauncher(){
    //when
    config.runJob();
    //then
    then(jobLauncher).should().run(eq(newsJob),any());
  }
}

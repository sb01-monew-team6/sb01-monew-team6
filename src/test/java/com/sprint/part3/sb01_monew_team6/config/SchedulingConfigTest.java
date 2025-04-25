package com.sprint.part3.sb01_monew_team6.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

@ExtendWith(MockitoExtension.class)
public class SchedulingConfigTest {
  @Mock
  private JobLauncher jobLauncher;
  @Mock
  private Job newsJob;
  @InjectMocks
  private SchedulingConfig config;


  @Test
  @DisplayName("collectNewsSchedule 호출시 collectAndSave 호출")
  void whenCollectNewsSchedule_thenCollectAndSave()
      throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    //given

    //when
    config.collectNewsSchedule();
    //then
    then(jobLauncher).should().run(eq(newsJob), any(JobParameters.class));
  }
}

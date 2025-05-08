package com.sprint.part3.sb01_monew_team6.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
class SchedulingConfigTest {

  @Mock private JobLauncher jobLauncher;
  @Mock private Job newsJob;
  @Mock private Job backupJob;

  @InjectMocks
  private SchedulingConfig schedulingConfig;

//  @Test
//  @DisplayName("collectNewsSchedule() 호출 시 newsJob 실행")
//  void collectNewsSchedule_executesNewsJob() throws Exception {
//    schedulingConfig.collectNewsSchedule();
//    verify(jobLauncher).run(eq(newsJob), any(JobParameters.class));
//  }

  @Test
  @DisplayName("collectNewsSchedule() 이미 완료된 잡 예외 무시")
  void collectNewsSchedule_ignoresAlreadyComplete() throws Exception {
    // any(Job.class) 로 stub 범위 확대
    doThrow(new JobInstanceAlreadyCompleteException("done"))
        .when(jobLauncher).run(any(Job.class), any(JobParameters.class));

    // should not throw
    schedulingConfig.collectNewsSchedule();

    // 그래도 newsJob 으로 한 번 호출된 것 검증
    verify(jobLauncher).run(eq(newsJob), any(JobParameters.class));
  }

//  @Test
//  @DisplayName("dailyBackup() 호출 시 backupJob 실행")
//  void dailyBackup_executesBackupJob() throws Exception {
//    schedulingConfig.dailyBackup();
//    verify(jobLauncher).run(eq(backupJob), any(JobParameters.class));
//  }
}
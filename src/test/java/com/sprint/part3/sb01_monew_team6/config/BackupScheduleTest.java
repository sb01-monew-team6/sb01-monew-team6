package com.sprint.part3.sb01_monew_team6.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;

@ExtendWith(MockitoExtension.class)
class BackupScheduleTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job backupJob;

  @InjectMocks
  private SchedulingConfig schedulingConfig;

  @Test
  @DisplayName("dailyBackup() 호출 시 backupJob 실행")
  void dailyBackup_executesBackupJob() throws Exception {
    // when
    schedulingConfig.dailyBackup();

    // then: backupJob과 함께 JobParameters가 전달됐는지 검증
    verify(jobLauncher).run(eq(backupJob), any(JobParameters.class));
  }

  @Test
  @DisplayName("dailyBackup() 이미 완료된 잡 예외 무시")
  void dailyBackup_ignoresAlreadyComplete() throws Exception {
    // given: JobInstanceAlreadyCompleteException이 던져지더라도
    willThrow(new JobInstanceAlreadyCompleteException("already done"))
        .given(jobLauncher).run(any(Job.class), any(JobParameters.class));

    // when & then: 예외가 바깥으로 터지지 않아야 함
    schedulingConfig.dailyBackup();

    // 그리고 정상적으로 backupJob에 한 번은 호출됐음을 검증
    verify(jobLauncher, times(1)).run(eq(backupJob), any(JobParameters.class));
  }
}
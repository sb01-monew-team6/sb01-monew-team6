package com.sprint.part3.sb01_monew_team6.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;

@ExtendWith(MockitoExtension.class)
class NewsScheduleTest {

  @Mock private JobLauncher jobLauncher;
  @Mock private Job newsJob;

  @InjectMocks
  private SchedulingConfig schedulingConfig;

  @Mock
  private JobExecution dummyExecution;

  @Test
  @DisplayName("collectNewsSchedule() 호출 시 newsJob 실행 및 완료 상태 확인")
  void collectNewsSchedule_executesNewsJob() throws Exception {
    // given: run()이 null 이 아닌 dummyExecution을 반환하도록 설정
    given(jobLauncher.run(eq(newsJob), any(JobParameters.class)))
        .willReturn(dummyExecution);
    given(dummyExecution.getStatus()).willReturn(BatchStatus.COMPLETED);
    given(dummyExecution.getExitStatus()).willReturn(ExitStatus.COMPLETED);

    // when
    schedulingConfig.collectNewsSchedule();

    // then: newsJob과 함께 JobParameters가 전달됐는지 검증
    verify(jobLauncher).run(eq(newsJob), any(JobParameters.class));
  }

  @Test
  @DisplayName("collectNewsSchedule() 이미 완료된 잡 예외 무시")
  void collectNewsSchedule_ignoresAlreadyComplete() throws Exception {
    // given: Exception 발생 시에도 메서드가 예외를 던지지 않도록
    willThrow(new JobInstanceAlreadyCompleteException("already done"))
        .given(jobLauncher).run(any(Job.class), any(JobParameters.class));

    // when & then: 예외가 바깥으로 터지지 않아야 함
    schedulingConfig.collectNewsSchedule();

    // 그리고 여전히 newsJob에 한 번은 호출됐음을 검증
    verify(jobLauncher, times(1)).run(eq(newsJob), any(JobParameters.class));
  }
}
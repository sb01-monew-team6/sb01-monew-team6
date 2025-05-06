package com.sprint.part3.sb01_monew_team6.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;

import com.sprint.part3.sb01_monew_team6.config.SchedulingConfig;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.awaitility.Awaitility;

@SpringBootTest(properties = {
    "cron.backup=0/5 * * * * *",
    "spring.batch.job.enabled=false"
})
@ActiveProfiles("test")
@EnableScheduling
class ArticleBackupSchedulerTest {

  @Autowired
  private SchedulingConfig scheduler;

  // real bean을 그대로 주입받음
  @Autowired
  @Qualifier("backupJob")
  private Job backupJob;

  // Scheduler가 호출하는 Launcher만 mock으로 대체
  @MockitoBean
  private JobLauncher jobLauncher;

  @BeforeEach
  void setUp() throws Exception {
    // jobLauncher.run(...) 만 stub
    JobExecution fakeExecution = new JobExecution(123L);
    given(jobLauncher.run(eq(backupJob), any(JobParameters.class)))
        .willReturn(fakeExecution);
  }

  @Test
  void 스케줄러가_jobLauncher를_주기적으로_호출한다() {
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(() ->
            then(jobLauncher).should(atLeast(2))
                .run(eq(backupJob), any(JobParameters.class))
        );
  }
}
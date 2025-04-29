package com.sprint.part3.sb01_monew_team6.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {
  private static final String CRON = "0 0 * * * *"; //매시간
  private final JobLauncher jobLauncher;
  private final Job newsJob;

  @Scheduled(cron = CRON)
  public void collectNewsSchedule(){
    // 실행마다 새로운 JobInstance 생성
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("time",System.currentTimeMillis())
        .toJobParameters();

    try{
      jobLauncher.run(newsJob,jobParameters);
    }catch (JobExecutionAlreadyRunningException
            | JobRestartException
            | JobInstanceAlreadyCompleteException
            | JobParametersInvalidException e) {
      log.error("배치 뉴스 수집 작업 실행 중 오류 발생", e);
      throw new RuntimeException("배치 작업 실행 실패", e);
    }

    log.info("스케줄러에 의해 배치 '{}' 작업이 성공적으로 실행되었습니다", newsJob.getName());
  }
}

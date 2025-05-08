package com.sprint.part3.sb01_monew_team6.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  //private static final String CRON_NEWS = "0 0 * * * *"; //매시간
  //private static final String CRON_BACKUP = "0 0 0 * * *"; //매일
  private final JobLauncher jobLauncher;
  private final Job newsJob;
  private final Job backupJob;

  @Scheduled(cron = "${cron.news}")
  public void collectNewsSchedule() {
    JobParameters params = new JobParametersBuilder()
        // timestamp 옵션 하나만 있어도 충분히 유니크합니다
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    try {
      jobLauncher.run(newsJob, params);
    } catch (JobInstanceAlreadyCompleteException e) {
      // 이미 완료된 경우엔 그냥 로그만 찍고 넘어가기
      log.warn("이미 완료된 뉴스 수집 잡 인스턴스가 있어서 건너뜁니다: run.id={}", params.getParameters().get("timestamp"));
      return;
    } catch (Exception e) {
      log.error("배치 뉴스 수집 작업 실행 중 오류 발생", e);
      throw new RuntimeException("배치 작업 실행 실패", e);
    }

    log.info("스케줄러에 의해 배치 '{}' 작업이 성공적으로 실행되었습니다", newsJob.getName());
  }

  @Scheduled(cron = "${cron.backup}")
  public void dailyBackup() throws Exception{
    LocalDate today = LocalDate.now();
    JobParameters parameter = new JobParametersBuilder()
        .addString("date",today.toString())
        .addLong("timestamp",System.currentTimeMillis())
        .toJobParameters();

    jobLauncher.run(backupJob,parameter);
    log.info("스케줄러에 의해 배치 '{}' 작업이 성공적으로 실행되었습니다", backupJob.getName());
  }
}

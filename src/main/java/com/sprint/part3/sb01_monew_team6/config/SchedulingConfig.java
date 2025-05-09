package com.sprint.part3.sb01_monew_team6.config;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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
  private final JobLauncher jobLauncher;
  private final Job newsJob;
  private final Job backupJob;

  @Scheduled(cron = "${cron.news}")
  public void collectNewsSchedule() {
    JobParameters params = new JobParametersBuilder() //매 실행마다 고유한 timestamp 파라미터를 만들어 잡 인스턴스 중복 방지
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    // 잡 실행 후 결과를 담을 변수
    JobExecution exec;
    try {
      exec = jobLauncher.run(newsJob, params);
    } catch (JobInstanceAlreadyCompleteException e) {
      // 이미 완료된 경우엔 그냥 로그만 찍고 넘어가기
      log.warn("이미 완료된 뉴스 수집 잡 인스턴스가 있어서 건너뜁니다: run.id={}", params.getParameters().get("timestamp"));
      return;
    } catch (Exception e) {
      log.error("배치 뉴스 수집 작업 실행 중 오류 발생", e);
      throw new RuntimeException("배치 작업 실행 실패", e);
    }
    // 실행 결과 상태에 따라 로그 레벨을 구분
    if (exec.getStatus() == BatchStatus.COMPLETED) {
      // 정상 완료일 때
      log.info("스케줄러에 의해 배치 '{}' 작업이 정상 완료되었습니다 (ExitCode={})",
          newsJob.getName(), exec.getExitStatus().getExitCode());
    } else {
      // 하나 이상의 스텝 오류 등으로 비정상 종료 시
      log.error("스케줄러에 의해 배치 '{}' 작업이 실패했습니다 (ExitStatus={})",
          newsJob.getName(), exec.getExitStatus());
    }
  }

  @Scheduled(cron = "${cron.backup}")
  public void dailyBackup() throws JobExecutionException {
    LocalDate today = LocalDate.now();
    JobParameters params = new JobParametersBuilder()
        .addString("date", today.toString())
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    try {
      jobLauncher.run(backupJob, params);
      log.info("스케줄러에 의해 배치 '{}' 작업이 성공적으로 실행되었습니다",
          backupJob.getName());
    } catch (JobInstanceAlreadyCompleteException e) {
      log.warn("이미 완료된 백업 잡 인스턴스가 있어서 건너뜁니다: date={}", today);
    }
    // JobExecutionAlreadyRunningException 등 기타 JobExecutionException 은
    // throws 절에 의해 그대로 전파
  }
}
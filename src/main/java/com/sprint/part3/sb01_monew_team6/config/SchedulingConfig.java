package com.sprint.part3.sb01_monew_team6.config;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {
  private final NewsCollectionService service;

  @Scheduled(cron = "0 0 * * * *") //매시간 마다 진행
  public void collectNewsSchedule(){
    service.collectAndSave();
  }
}

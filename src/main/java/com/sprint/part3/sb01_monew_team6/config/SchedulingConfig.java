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
  private static final String CRON = "0 0 0 * * *";

  @Scheduled(cron = CRON)
  public void collectNewsSchedule(){
    service.collectAndSave();
  }
}

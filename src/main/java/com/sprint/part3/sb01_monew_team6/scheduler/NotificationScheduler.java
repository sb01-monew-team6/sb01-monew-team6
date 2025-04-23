package com.sprint.part3.sb01_monew_team6.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final NotificationService notificationService;

	@Scheduled(cron = "0 0 9 * * Mon")
	public void deleteConfirmedNotificationsOlderThanWeek() {
		notificationService.deleteAll();
	}

}

package com.sprint.part3.sb01_monew_team6.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final NotificationService notificationService;

	@Value("${notification.batch.size}")
	private int batchSize;

	@Scheduled(cron = "0 0 0 * * *")
	public void deleteConfirmedNotificationsOlderThanWeek() {

		Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

		Limit limit = Limit.of(batchSize);

		while (true) {
			int deletedCount = notificationService.deleteAllOlderThanWeek(weekAgo, limit);

			if (deletedCount == 0) break;
		}
	}

}

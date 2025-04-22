package com.sprint.part3.sb01_monew_team6.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.repository.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Test
	@DisplayName("findAllByUserId 정상 호출 시 정상 조회 반환")
	public void throwNotificationExceptionWhenUserIdNonExistWhileFindAllByUserId() throws Exception {
		//given
		Long userId = 1L;
		Instant createdAt = Instant.parse("2025-04-22T00:00:00Z");
		Pageable pageable = PageRequest.of(0, 50, DESC, "createdAt");

		Notification notification = Notification.createNotification(
			new User(),
			"hello",
			ResourceType.COMMENT,
			1L,
			false
		);
		when(notificationRepository.findAllByUserId(eq(userId), any(), any())).thenReturn(List.of(notification));

		//when
		PageResponse<NotificationDto> notifications = notificationService.findAllByUserId(userId, createdAt, pageable);

		//then
		assertThat(notifications.contents().size()).isEqualTo(1);
		assertThat(notifications.size()).isEqualTo(50);
		assertThat(notifications.hasNext()).isFalse();
		assertThat(notifications.nextCursor()).isEqualTo(createdAt.toString());
		assertThat(notifications.nextAfter()).isEqualTo(createdAt.toString());
		assertThat(notifications.totalElements()).isEqualTo(1);

	}
}
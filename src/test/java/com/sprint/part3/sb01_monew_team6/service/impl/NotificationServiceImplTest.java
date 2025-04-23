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
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationException;
import com.sprint.part3.sb01_monew_team6.mapper.NotificationMapper;
import com.sprint.part3.sb01_monew_team6.mapper.PageResponseMapper;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.repository.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private NotificationMapper notificationMapper;
	@Mock
	private PageResponseMapper pageResponseMapper;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Test
	@DisplayName("findAllByUserId 정상 호출 시 정상 조회 반환")
	public void findAllByUserIdSuccessfully() throws Exception {
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
		NotificationDto notificationDto = new NotificationDto(
			1L,
			createdAt,
			Instant.now(),
			false,
			userId,
			"hello",
			ResourceType.COMMENT,
			1L
		);
		Slice<Notification> slice = new SliceImpl<>(
			List.of(notification),
			pageable,
			false
		);
		Slice<NotificationDto> sliceDto = new SliceImpl<>(
			List.of(notificationDto),
			pageable,
			false
		);
		PageResponse<NotificationDto> pageResponse = new PageResponse<>(
			sliceDto.getContent(),
			createdAt,
			createdAt,
			slice.getSize(),
			slice.hasNext(),
			1L
		);

		when(userRepository.existsByIdAndIsDeletedFalse(userId))
			.thenReturn(true);
		when(notificationRepository.countByUserIdAndConfirmedFalse(userId))
			.thenReturn(1L);
		when(notificationRepository.findAllByUserId(eq(userId), any(), any(), any()))
			.thenReturn(slice);
		when(notificationMapper.toDto(any(Notification.class)))
			.thenReturn(notificationDto);
		when(pageResponseMapper.fromSlice(any(Slice.class), any(), any(), any()))
			.thenReturn(pageResponse);

		//when
		PageResponse<NotificationDto> notifications = notificationService.findAllByUserId(userId, createdAt, createdAt,
			pageable);

		//then // 상태 기반 검증
		assertThat(notifications.contents().size()).isEqualTo(1);
		assertThat(notifications.size()).isEqualTo(50);
		assertThat(notifications.hasNext()).isFalse();
		assertThat(notifications.nextCursor()).isEqualTo(createdAt);
		assertThat(notifications.nextAfter()).isEqualTo(createdAt);
		assertThat(notifications.totalElements()).isEqualTo(1);

	}

	@Test
	@DisplayName("findAllByUserId 호출 시 userId 가 존재하지 않는다면 NotificationException 발생")
	public void throwNotificationExceptionWhenUserIdNonExistWhileFindAllByUserId() throws Exception {
		//given
		Long userId = 2L;
		Instant createdAt = Instant.parse("2025-04-22T00:00:00Z");
		Pageable pageable = PageRequest.of(0, 50, DESC, "createdAt");

		when(userRepository.existsByIdAndIsDeletedFalse(2L)).thenReturn(false);

		//when & then
		assertThatThrownBy(() ->
			notificationService.findAllByUserId(userId, createdAt, createdAt, pageable)
		).isInstanceOf(NotificationException.class);
	}

	@Test
	@DisplayName("updateAllByUserId 정상 호출 시 정상 값 반환")
	public void updateAllByUserIdSuccessfully() throws Exception {
		//given
		Long userId = 1L;

		when(userRepository.existsByIdAndIsDeletedFalse(userId)).thenReturn(true);
		doNothing().when(notificationRepository).updateAllByUserId(eq(userId));

		//when
		notificationService.updateAllByUserId(userId);

		//then // 행위 기반 검증
		verify(notificationRepository).updateAllByUserId(eq(userId));
	}

	@Test
	@DisplayName("updateAllByUserId 호출 시 userId 가 존재하지 않는다면 NotificationException 발생")
	public void throwNotificationExceptionWhenUserIdNonExistWhileUpdateAllByUserId() throws Exception {
		//given
		Long userId = 1L;

		when(userRepository.existsByIdAndIsDeletedFalse(userId)).thenReturn(false);

		//when & then
		assertThatThrownBy(() ->
			notificationService.updateAllByUserId(userId)
		).isInstanceOf(NotificationException.class);
	}

	@Test
	@DisplayName("updateByUserId 호출 시 userId 가 존재하지 않는다면 NotificationException 발생")
	public void throwNotificationExceptionWhenUserIdNonExistWhileUpdateByUserId() throws Exception {
		//given
		Long userId = 1L;
		Long notificationId = 1L;

		when(userRepository.existsByIdAndIsDeletedFalse(userId)).thenReturn(false);

		//when & then
		assertThatThrownBy(() ->
			notificationService.updateByUserId(userId, notificationId)
		).isInstanceOf(NotificationException.class);
	}

	@Test
	@DisplayName("updateByUserId 호출 시 notificationId 가 존재하지 않는다면 NotificationException 발생")
	public void throwNotificationExceptionWhenNotificationIdNonExistWhileUpdateByUserId() throws Exception {
		//given
		Long userId = 1L;
		Long notificationId = 1L;

		when(userRepository.existsByIdAndIsDeletedFalse(userId)).thenReturn(true);
		when(notificationRepository.existsById(notificationId)).thenReturn(false);

		//when & then
		assertThatThrownBy(() ->
			notificationService.updateByUserId(userId, notificationId)
		).isInstanceOf(NotificationException.class);
	}

	@Test
	@DisplayName("updateByUserId 정상 호출 시 정상 값 반환")
	public void updateByUserIdSuccessfully() throws Exception {
		//given
		Long userId = 1L;
		Long notificationId = 1L;

		when(userRepository.existsByIdAndIsDeletedFalse(userId)).thenReturn(true);
		when(notificationRepository.existsById(notificationId)).thenReturn(true);
		doNothing().when(notificationRepository).updateByUserId(eq(userId), eq(notificationId));

		//when
		notificationService.updateByUserId(userId, notificationId);

		//then // 행위 기반 검증
		verify(notificationRepository).updateByUserId(eq(userId), eq(notificationId));
	}

	@Test
	@DisplayName("deleteAll 정상 호출 시 정상 값 반환")
	public void deleteAllOlderThanWeekSuccessfully() throws Exception {
		//given
		doNothing().when(notificationRepository).deleteAllOlderThanWeek(any());

		//when
		notificationService.deleteAllOlderThanWeek();

		//then // 행위 기반 검증
		verify(notificationRepository, times(1)).deleteAllOlderThanWeek(any());
	}
}
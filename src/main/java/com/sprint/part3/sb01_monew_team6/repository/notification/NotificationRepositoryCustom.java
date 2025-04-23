package com.sprint.part3.sb01_monew_team6.repository.notification;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.sprint.part3.sb01_monew_team6.entity.Notification;

public interface NotificationRepositoryCustom {

	Slice<Notification> findAllByUserId(Long userId, Instant createdAt, Pageable pageable);
}

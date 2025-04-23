package com.sprint.part3.sb01_monew_team6.repository.notification;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sprint.part3.sb01_monew_team6.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

	@Override
	Slice<Notification> findAllByUserId(Long userId, Instant createdAt, Pageable pageable);

	@Query(value = """
			SELECT COUNT(n) FROM Notification n
			WHERE (n.user.id = :userId)
				AND (n.confirmed = false)
		""")
	long countByUserIdAndConfirmedFalse(Long userId);

	void updateAllByUserId(Long userId);
}
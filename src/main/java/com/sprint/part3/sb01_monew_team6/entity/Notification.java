package com.sprint.part3.sb01_monew_team6.entity;

import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import com.sprint.part3.sb01_monew_team6.convertor.ResourceTypeConverter;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Notification extends BaseUpdatableEntity {

	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String content;

	@Convert(converter = ResourceTypeConverter.class)
	@Column(nullable = false)
	private ResourceType resourceType;

	@Column(nullable = false)
	private Long resourceId;

	@Column(nullable = false)
	private boolean confirmed;

	private Notification(User user, String content, ResourceType resourceType, Long resourceId, boolean confirmed) {
		this.user = user;
		this.content = content;
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.confirmed = confirmed;
	}

	public static Notification createNotification(User user, String content, ResourceType resourceType, Long resourceId,
		boolean confirmed) throws NotificationDomainException {

		if (Objects.isNull(content)) {
			throw new NotificationDomainException("내용이 null 일 수 없습니다.", Map.of("content", "null"));
		}

		return new Notification(user, content, resourceType, resourceId, confirmed);
	}
}

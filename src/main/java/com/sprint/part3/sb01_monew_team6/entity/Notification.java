package com.sprint.part3.sb01_monew_team6.entity;

import static jakarta.persistence.FetchType.*;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import com.sprint.part3.sb01_monew_team6.entity.convertor.ResourceTypeConverter;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "notifications")
@Getter
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
}

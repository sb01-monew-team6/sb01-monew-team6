package com.sprint.part3.sb01_monew_team6.entity.base;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity {

	@LastModifiedDate
	Instant updatedAt;
}

package com.sprint.part3.sb01_monew_team6.entity.base;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.Getter;

@Getter
public abstract class BaseDocument {

	@Id
	private String id;

	@CreatedDate
	private Instant createdAt;
}

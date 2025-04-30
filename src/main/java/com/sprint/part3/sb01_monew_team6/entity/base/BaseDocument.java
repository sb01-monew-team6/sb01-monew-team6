package com.sprint.part3.sb01_monew_team6.entity.base;

import jakarta.persistence.Id;
import lombok.Getter;

@Getter
public abstract class BaseDocument {

	@Id
	private String id;
}

package com.sprint.part3.sb01_monew_team6.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

class NotificationTest {

	@Test
	@DisplayName("알림 생성 시 content 필드가 null 이면 NotificationException 발생")
	public void throwExceptionWhenContentIsNull() throws Exception {
		//given
		String content = null;

		//when & then
		assertThatThrownBy(
			new Notification(user, null, ResourceType.COMMENT, 1L, false)
		).isInstanceOf(NotificationException.class);
	}

}
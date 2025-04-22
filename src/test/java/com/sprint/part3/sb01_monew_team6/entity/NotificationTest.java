package com.sprint.part3.sb01_monew_team6.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;
import com.sprint.part3.sb01_monew_team6.exception.notification.NotificationDomainException;

class NotificationTest {

	@Test
	@DisplayName("알림 생성 시 content 필드가 null 이면 NotificationDomainException 발생")
	public void throwExceptionWhenContentIsNull() throws Exception {
		//given
		String content = null;

		//when & then
		assertThatThrownBy(() ->
			Notification.createNotification(new User(), content, ResourceType.COMMENT, 1L, false)
		).isInstanceOf(NotificationDomainException.class);
	}

	@Test
	@DisplayName("알림 생성 시 user 필드가 null 이면 NotificationDomainException 발생")
	public void throwExceptionWhenUserIsNull() throws Exception {
		//given
		User user = null;

		//when & then
		assertThatThrownBy(() ->
			Notification.createNotification(user, "", ResourceType.COMMENT, 1L, false)
		).isInstanceOf(NotificationDomainException.class);
	}

	@Test
	@DisplayName("알림 생성 시 resourceType 필드가 null 이면 NotificationDomainException 발생")
	public void throwExceptionWhenResourceTypeIsNull() throws Exception {
		//given
		ResourceType resourceType = null;

		//when & then
		assertThatThrownBy(() ->
			Notification.createNotification(new User(), "", resourceType, 1L, false)
		).isInstanceOf(NotificationDomainException.class);
	}

	@Test
	@DisplayName("알림 생성 시 resourceId 필드가 null 이면 NotificationDomainException 발생")
	public void throwExceptionWhenResourceIdIsNull() throws Exception {
		//given
		Long resourceId = null;

		//when & then
		assertThatThrownBy(() ->
			Notification.createNotification(new User(), "", ResourceType.COMMENT, resourceId, false)
		).isInstanceOf(NotificationDomainException.class);
	}

	@Test
	@DisplayName("알림 생성 시 content 필드가 빈 값이면 NotificationDomainException 발생")
	public void throwExceptionWhenContentIsBlank() throws Exception {
		//given
		String content = "   ";

		//when & then
		assertThatThrownBy(() ->
			Notification.createNotification(new User(), content, ResourceType.COMMENT, 1L, false)
		).isInstanceOf(NotificationDomainException.class);
	}
}
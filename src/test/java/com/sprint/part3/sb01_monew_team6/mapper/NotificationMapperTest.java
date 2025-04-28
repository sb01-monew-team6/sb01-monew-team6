package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

class NotificationMapperTest {

	private final NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);

	@Test
	@DisplayName("toDto 정상 호출 시 엔티티가 dto 로 정상 변환")
	public void returnDtoWhenToDtoSuccessfullyCalled() throws Exception {
	    //given
		User user = new User("email@email.com", "nickname", "1234");
		Notification notification = Notification.createNotification(
			user,
			"hello",
			ResourceType.COMMENT,
			1L,
			false
		);

	    //when
		NotificationDto dto = notificationMapper.toDto(notification);

		//then
		assertThat(dto.content()).isEqualTo("hello");
		assertThat(dto.resourceType()).isEqualTo(ResourceType.COMMENT);
		assertThat(dto.resourceId()).isEqualTo(1);
		assertThat(dto.confirmed()).isFalse();
	}
}
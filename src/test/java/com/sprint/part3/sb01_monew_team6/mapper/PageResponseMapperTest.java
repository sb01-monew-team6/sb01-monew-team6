package com.sprint.part3.sb01_monew_team6.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.SliceImpl;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;
import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

class PageResponseMapperTest {

	private final NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);
	private final PageResponseMapper pageResponseMapper = Mappers.getMapper(PageResponseMapper.class);

	@Test
	@DisplayName("fromSlice 정상 호출 시 Slice 가 PageResponse 로 정상 변환")
	public void returnPageResponseWhenFromSliceSuccessfullyCalled() throws Exception {
		//given
		User user = new User("email@email.com", "nickname", "1234");
		Notification notification = Notification.createNotification(
			user,
			"hello",
			ResourceType.COMMENT,
			1L,
			false
		);

		NotificationDto dto = notificationMapper.toDto(notification);
		SliceImpl<NotificationDto> slice = new SliceImpl<>(List.of(dto));

		//when
		PageResponse<NotificationDto> pageResponse = pageResponseMapper.fromSlice(
			slice,
			null,
			null,
			1L
		);

		//then
		assertThat(pageResponse.contents().size()).isEqualTo(1);
		assertThat(pageResponse.contents().get(0).content()).isEqualTo("hello");
		assertThat(pageResponse.totalElements()).isEqualTo(1L);
	}
}
package com.sprint.part3.sb01_monew_team6.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.part3.sb01_monew_team6.dto.notification.NotificationDto;
import com.sprint.part3.sb01_monew_team6.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

	@Mapping(target = "userId", source = "user.id")
	NotificationDto toDto(Notification notification);
}

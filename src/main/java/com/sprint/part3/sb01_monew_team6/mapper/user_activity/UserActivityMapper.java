package com.sprint.part3.sb01_monew_team6.mapper.user_activity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.UserActivityDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {

	@Mapping(target = "id", source = "userId")
	@Mapping(target = "createdAt", source = "userCreatedAt")
	UserActivityDto toDto(UserActivity userActivity);
}

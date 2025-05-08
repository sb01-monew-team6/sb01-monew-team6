package com.sprint.part3.sb01_monew_team6.mapper.user_activity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueMappingStrategy;

import com.sprint.part3.sb01_monew_team6.dto.UserDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.UserActivityDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

@Mapper(
	componentModel = "spring",
	nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface UserActivityMapper {

	@Mapping(target = "id", source = "userId")
	@Mapping(target = "createdAt", source = "userCreatedAt")
	UserActivityDto toDto(UserActivity userActivity);

	@Mapping(target = "userId", source = "id")
	@Mapping(target = "userCreatedAt", source = "createdAt")
	@Mapping(target = "subscriptions", ignore = true)
	@Mapping(target = "comments", ignore = true)
	@Mapping(target = "commentLikes", ignore = true)
	@Mapping(target = "articleViews", ignore = true)
	UserActivity fromUserDto(UserDto userDto);
}

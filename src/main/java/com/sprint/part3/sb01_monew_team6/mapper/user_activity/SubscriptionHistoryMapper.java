package com.sprint.part3.sb01_monew_team6.mapper.user_activity;

import org.mapstruct.Mapper;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.SubscriptionHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

@Mapper(componentModel = "spring")
public interface SubscriptionHistoryMapper {

	UserActivity.SubscriptionHistory fromDto(SubscriptionHistoryDto dto);
}

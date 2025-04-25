package com.sprint.part3.sb01_monew_team6.mapper.user_activity;

import org.mapstruct.Mapper;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentLikeHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

@Mapper(componentModel = "spring")
public interface CommentLikeHistoryMapper {

	UserActivity.CommentLikeHistory fromDto(CommentLikeHistoryDto dto);
}

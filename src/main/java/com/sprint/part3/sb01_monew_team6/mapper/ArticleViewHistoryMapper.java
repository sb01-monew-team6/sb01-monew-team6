package com.sprint.part3.sb01_monew_team6.mapper;

import org.mapstruct.Mapper;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

@Mapper(componentModel = "spring")
public interface ArticleViewHistoryMapper {

	UserActivity.ArticleViewHistory fromDto(ArticleViewHistoryDto dto);
}

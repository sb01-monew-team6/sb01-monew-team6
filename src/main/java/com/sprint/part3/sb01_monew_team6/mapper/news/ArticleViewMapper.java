package com.sprint.part3.sb01_monew_team6.mapper.news;

import com.sprint.part3.sb01_monew_team6.dto.news.ArticleViewDto;
import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {
  ArticleViewDto toDto(ArticleView articleView);
}

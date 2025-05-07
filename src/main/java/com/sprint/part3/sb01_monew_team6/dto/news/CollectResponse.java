package com.sprint.part3.sb01_monew_team6.dto.news;

import java.util.List;
import lombok.Builder;

@Builder
public record CollectResponse(
    String message,
    int count,
    List<ArticleDto> articles
) {

}

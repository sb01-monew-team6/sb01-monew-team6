package com.sprint.part3.sb01_monew_team6.dto.news;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record ArticleRestoreResultDto(
    LocalDate restoreDate,
    List<Long> restoredArticleIds,
    Long restoredArticleCount
) {

}

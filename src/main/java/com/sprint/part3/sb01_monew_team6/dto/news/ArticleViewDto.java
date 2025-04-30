package com.sprint.part3.sb01_monew_team6.dto.news;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ArticleViewDto(
    Long id,
    Long viewedBy,
    Instant createdAt,
    Long articleId,
    String source,
    String sourceUrl,
    String articleTitle,
    String articlePublishedDate,
    String articleSummary,
    Long articleCommentCount,
    Long articleViewCount
) {

}

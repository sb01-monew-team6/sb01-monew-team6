package com.sprint.part3.sb01_monew_team6.dto.news;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseArticleDto(
  String keyword,
  Long interestId,
  List<String> sourceIn,
  Instant publishDateFrom,
  Instant publishDateTo,
  String orderBy,
  String direction,
  Long cursor,
  Instant after
) {

}

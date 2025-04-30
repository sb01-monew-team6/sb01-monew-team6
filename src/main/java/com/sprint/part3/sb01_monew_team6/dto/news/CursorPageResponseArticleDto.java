package com.sprint.part3.sb01_monew_team6.dto.news;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseArticleDto(
    List<ArticleDto> content,    // 조회된 Article 목록
    String nextCursor,           // 다음 페이지를 위한 커서 값
    Instant nextAfter,           // 다음 페이지를 위한 after 타임스탬프
    int size,                    // 현재 페이지 항목 수
    Long totalElements,          // 전체 데이터 건수
    boolean hasNext              // 다음 페이지 존재 여부
) {
  public static CursorPageResponseArticleDto toDto(
      List<ArticleDto> content,
      String nextCursor,
      Instant nextAfter,
      int size,
      Long totalElements,
      boolean hasNext
  ) {
    return new CursorPageResponseArticleDto(
        content,
        nextCursor,
        nextAfter,
        size,
        totalElements,
        hasNext
    );
  }
}

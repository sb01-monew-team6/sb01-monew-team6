package com.sprint.part3.sb01_monew_team6.dto.news;

import java.util.List;

public record CursorPageResponseArticleDto<T>( //<T> 페이지 항목의 타입
    List<T> content, // 페이지 내용
    String nextCursor, //다음 페이지를 조회할 커서 문자열
    String nextAfter, //다음 페이지 시작 시각 (ISO-8601 문자열)
    int size, //페이지 크기
    long totalElements, //전체 요소 수
    boolean hasNext //다음 페이지 존재 여부
) {

}

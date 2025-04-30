package com.sprint.part3.sb01_monew_team6.dto.news;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageRequestArticleDto(
    String keyword, //검색어
    Long interestId, //관심사 ID
    List<String> sourceIn, //출처
    Instant publishDateFrom, //날짜 시작(범위)
    Instant publishDateTo, //날짜 끝(범위)
    @NotNull @Pattern(regexp = "publishDate|title|commentCount|viewCount")
    String orderBy, //정렬 속성 이름
    @NotNull @Pattern(regexp = "ASC|DESC")
    String direction, //정렬 방향 (ASC, DESC)
    String cursor, //커서 값
    Instant after, //보조 커서(createdAt) 값
    @NotNull @Min(1)
    Integer limit //커서 페이지 크기
    //Long Monew_Request_User_ID //요청자 ID
) {

}

package com.sprint.part3.sb01_monew_team6.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({
    "content",
    "nextCursor",
    "nextAfter",
    "size",
    "totalElements",
    "hasNext"
})
public class CursorPageResponseInterestDto {

  @JsonProperty("content")
  private final List<InterestDto> content;

  @JsonProperty("nextCursor")
  private final String nextCursor;

  @JsonProperty("nextAfter")
  private final String nextAfter;

  @JsonProperty("size")
  private final int size;

  @JsonProperty("totalElements")
  private final long totalElements;

  @JsonProperty("hasNext")
  private final boolean hasNext;
}

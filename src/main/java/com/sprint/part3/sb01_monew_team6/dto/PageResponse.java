package com.sprint.part3.sb01_monew_team6.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageResponse<T>(
		/**
		 JSON 직렬화(serialization) 시엔 "content" 로,
		 역직렬화(deserialization) 시엔 "content" 또는 "contents" 둘 다 허용*/@JsonProperty("content")@JsonAlias("contents")
		List<T> contents,
		Object nextCursor,
		Object nextAfter,
		int size,
		boolean hasNext,
		Long totalElements
) {
}
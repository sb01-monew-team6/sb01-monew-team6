package com.sprint.part3.sb01_monew_team6.dto;

import java.util.List;

public record PageResponse<T>(
	List<T> contents,
	Object nextCursor,
	int size,
	boolean hasNext,
	Long totalElements
) {
}

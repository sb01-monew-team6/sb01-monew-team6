package com.sprint.part3.sb01_monew_team6.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Slice;

import com.sprint.part3.sb01_monew_team6.dto.PageResponse;

@Mapper(componentModel = "spring")
public interface PageResponseMapper {

	default <T> PageResponse<T> fromSlice(Slice<T> slice, Object nextCursor, Object nextAfter, Long totalElements) {
		return new PageResponse<>(
			slice.getContent(),
			nextCursor,
			nextAfter,
			slice.getSize(),
			slice.hasNext(),
			totalElements
		);
	}
}

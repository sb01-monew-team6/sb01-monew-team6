package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CursorPageResponseInterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestUpdateRequestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestCreateRequestDto;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface InterestService {

  Interest createInterest(String name, List<String> keywords);

  Interest updateInterest(Long interestId, InterestUpdateRequestDto requestDto);

  void deleteInterest(Long interestId);

  /**
   * Cursor 기반 페이지 조회
   * @param userId       요청 사용자 ID
   * @param keyword      검색 키워드
   * @param cursorId     마지막 항목 ID
   * @param cursorValue  마지막 항목 정렬 값
   * @param orderBy      정렬 대상 컬럼 (name | subscriberCount | createdAt)
   * @param direction    정렬 방향 (ASC|DESC)
   * @param limit        페이지 크기
   */
  CursorPageResponseInterestDto findAll(
      Long userId,
      String keyword,
      Long cursorId,
      String cursorValue,
      String orderBy,
      Sort.Direction direction,
      int limit
  );
}

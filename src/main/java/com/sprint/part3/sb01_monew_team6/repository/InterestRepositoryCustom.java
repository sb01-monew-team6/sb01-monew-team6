package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface InterestRepositoryCustom {

  /**
   * 키워드(이름 또는 keywords 문자열) 및 커서 기반 페이지네이션으로 Interest 목록 조회
   * @param keyword 검색어 (null 가능)
   * @param pageable 정렬 정보 및 페이지 크기 포함 (size)
   * @param cursorId 다음 페이지 조회를 위한 마지막 항목의 ID (첫 페이지는 null)
   * @param cursorValue 다음 페이지 조회를 위한 마지막 항목의 주 정렬 컬럼 값 (첫 페이지는 null)
   * @return Slice<Interest> (콘텐츠 목록 + 다음 페이지 유무 정보)
   */
  Slice<Interest> searchWithCursor(String keyword, Pageable pageable, Long cursorId, Object cursorValue); // <<<--- cursorValue 파라미터 추가

}

package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.CursorPageResponseInterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestCreateRequestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestDto;
import com.sprint.part3.sb01_monew_team6.dto.InterestUpdateRequestDto;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
@Slf4j
public class InterestController {

  private final InterestService interestService;

  @PostMapping
  public ResponseEntity<InterestDto> createInterest(
      @Valid @RequestBody InterestCreateRequestDto requestDto
  ) {
    log.info("Attempting to create interest with name: {}", requestDto.name());
    Interest createdInterest = interestService.createInterest(requestDto.name(), requestDto.keywords());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(InterestDto.fromEntity(createdInterest, false));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CursorPageResponseInterestDto> getInterests(
      @RequestHeader("Monew-Request-User-ID") Long userId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(required = false) String cursorValue,
      @RequestParam(defaultValue = "name") String orderBy,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(defaultValue = "10") int limit
  ) {
    log.info("Fetching interests with keyword: {}, cursorId: {}, cursorValue: {}, orderBy: {}, direction: {}, limit: {}",
        keyword, cursorId, cursorValue, orderBy, direction, limit);

    CursorPageResponseInterestDto dto = interestService.findAll(
        userId,
        keyword,
        cursorId,
        cursorValue,
        orderBy,
        direction,
        limit
    );

    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{interestId}")
  public ResponseEntity<Void> deleteInterest(@PathVariable Long interestId) {
    log.info("Attempting to delete interest with ID: {}", interestId);
    interestService.deleteInterest(interestId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{interestId}")
  public ResponseEntity<InterestDto> updateInterest(
      @PathVariable Long interestId,
      @Valid @RequestBody InterestUpdateRequestDto requestDto
  ) {
    log.info("Updating interest {} with {}", interestId, requestDto);
    Interest updatedInterest = interestService.updateInterest(interestId, requestDto);
    return ResponseEntity.ok(InterestDto.fromEntity(updatedInterest, false));
  }

}

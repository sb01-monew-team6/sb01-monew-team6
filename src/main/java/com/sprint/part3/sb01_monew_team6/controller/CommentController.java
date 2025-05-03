package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.dto.CommentUpdateRequest;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> registerComment(@Valid @RequestBody CommentRegisterRequest request) {
        CommentDto commentDto = commentService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
    }

    @GetMapping
    public ResponseEntity<?> getComments(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String after,
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = "Monew-Request-User-ID", required = false) Long requestUserId
    ) {
        if (orderBy == null || direction == null || limit == null || requestUserId == null) {
            return ResponseEntity.badRequest().build(); //  400 Bad Request
        }

        //  orderBy 값 검증 추가
        if (!orderBy.equals("createdAt") && !orderBy.equals("likeCount")) {
            return ResponseEntity.badRequest().build();
        }

        //  direction 검증 추가
        if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
            return ResponseEntity.badRequest().build();
        }

        List<CommentDto> commentList = commentService.findAll(
                articleId, orderBy, direction, cursor, after, limit, requestUserId
        );

        // 응답 형태를 content라는 키에 담아서 반환
        return ResponseEntity.ok()
                .body(Map.of("content", commentList));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id){
        log.info("[CommentController] 사용자 논리 삭제 요청: CommentId : {}",id);
        commentService.softDeleteComment(id);
        log.info("[CommentController] 사용자 논리 삭제 요청 성공: CommentId : {}",id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
        @PathVariable Long commentId,
        @RequestHeader("Monew-Request-User-ID") Long userId,
        @RequestBody @Valid CommentUpdateRequest request
    ) {
        CommentDto updatedComment = commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(updatedComment);
    }


}

package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.*;
import com.sprint.part3.sb01_monew_team6.service.CommentLikeService;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<CommentDto> registerComment(@Valid @RequestBody CommentRegisterRequest request) {
        CommentDto commentDto = commentService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String after,
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = "Monew-Request-User-ID", required = false) Long requestUserId
    ) {
        // 잘못된 articleId 값 처리: articleId가 null일 때는 모든 댓글을 조회하도록 설정
        if(articleId != null && articleId < 1) {
            return ResponseEntity.badRequest().build(); // 잘못된 articleId에 대해 400 반환
        }

        // 잘못된 limit 값 처리: limit이 0 이하일 경우 400 반환
        if(limit != null && limit <= 0) {
            return ResponseEntity.badRequest().build(); // 잘못된 limit 값에 대해 400 반환
        }

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

        PageResponse<CommentDto> commentPage = commentService.findAll(
                articleId, orderBy, direction, cursor, after, limit, requestUserId
        );

        return ResponseEntity.ok(commentPage);
    }

    @PostMapping("/{commentId}/comment-likes")
    public ResponseEntity<CommentLikeDto> likeComment(@PathVariable Long commentId,
                                                      @RequestHeader("Monew-Request-User-ID") Long userId
    ) {
        CommentLikeDto dto = commentService.likeComment(commentId, userId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{commentId}/comment-likes")
    public ResponseEntity<Void> cancelCommentLike(@PathVariable Long commentId, @RequestHeader("Monew-Request-User-ID") Long userId) {
        log.info("[cancelCommentLike] 댓글 좋아요 취소 요청: commentId={}, userId={}", commentId, userId);
        commentLikeService.cancelLike(commentId, userId);
        return ResponseEntity.ok().build();
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

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable Long commentId) {
        commentService.hardDelete(commentId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}

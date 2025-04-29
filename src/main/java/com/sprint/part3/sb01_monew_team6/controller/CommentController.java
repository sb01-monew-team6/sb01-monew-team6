package com.sprint.part3.sb01_monew_team6.controller;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> getComments(
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

        return ResponseEntity.ok().build(); // 나중에 실제 구현
    }
}

package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.*;

import java.util.List;

public interface CommentService {

    CommentDto register(CommentRegisterRequest request);

    PageResponse<CommentDto> findAll(
            Long articleId,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit,
            Long requestUserId
    );

    CommentLikeDto likeComment(Long commentId, Long userId);

    void softDeleteComment(Long id);

    CommentDto updateComment(Long commentId, Long userId, CommentUpdateRequest request);

    void hardDelete(Long commentId);
}

package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;

import com.sprint.part3.sb01_monew_team6.dto.CommentUpdateRequest;
import java.util.List;

public interface CommentService {

    CommentDto register(CommentRegisterRequest request);

    List<CommentDto> findAll(
            Long articleId,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit,
            Long requestUserId
    );

    List<CommentDto> getComments(Long articleId);

    void softDeleteComment(Long id);

    CommentDto updateComment(Long commentId, Long userId, CommentUpdateRequest request)
;}

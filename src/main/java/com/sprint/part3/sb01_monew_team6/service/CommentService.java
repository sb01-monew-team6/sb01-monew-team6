package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;

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
}

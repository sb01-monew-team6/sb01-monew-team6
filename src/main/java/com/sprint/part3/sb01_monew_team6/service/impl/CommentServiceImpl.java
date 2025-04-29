package com.sprint.part3.sb01_monew_team6.service.impl;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Override
    public CommentDto register(CommentRegisterRequest request) {
        // 일단 임시로 아무거나 리턴 (테스트 통과용)
        return null;
    }

    @Override
    public List<CommentDto> findAll(
            Long articleId,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit,
            Long requestUserId
    ) {
        // 일단 임시로 아무거나 리턴 (테스트 통과용)
        return null;
    }
}

package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentLikeDto;

public interface CommentLikeService {

    CommentLikeDto likeComment(Long commentId, Long userId);
}

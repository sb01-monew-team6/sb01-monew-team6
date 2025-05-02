package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 댓글에 대한 좋아요 수 카운트
    long countByCommentId(Long commentId);
    
    // 특정 댓글에 대해 사용자가 좋아요를 눌렀는지 여부 확인
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}

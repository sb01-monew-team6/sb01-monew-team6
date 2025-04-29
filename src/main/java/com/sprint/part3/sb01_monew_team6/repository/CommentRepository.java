package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {

  //특정 articleId 의 댓글 수 조회
  long countByArticleId(Long articleId);

  List<Comment> findAllByArticleId(Long articleId);
}

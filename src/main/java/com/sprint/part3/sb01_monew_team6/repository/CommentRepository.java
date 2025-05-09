package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {

  //특정 articleId 의 댓글 수 조회
  long countByArticleIdAndIsDeletedFalse(Long articleId);

  @Query("""
          SELECT c FROM Comment c
          JOIN FETCH c.article art
          JOIN FETCH c.user
          WHERE art.id = :articleId AND c.isDeleted = false
      """)
  List<Comment> findAllByArticleId(Long articleId);

  @Query("""
      SELECT c FROM Comment c
      JOIN FETCH c.article
      LEFT JOIN FETCH c.commentLikes
      JOIN FETCH c.user
      WHERE c.id = :id AND c.isDeleted = false
      """)
  Optional<Comment> findByIdWithArticleAndCommentLikesAndUser(Long id);

  @Query("""
      SELECT c FROM Comment c
      JOIN FETCH c.article
      JOIN FETCH c.user
      WHERE c.id = :id AND c.isDeleted = false
      """)
  Optional<Comment> findByIdWithArticleAndUser(Long id);
}

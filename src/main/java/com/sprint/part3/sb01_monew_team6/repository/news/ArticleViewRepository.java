package com.sprint.part3.sb01_monew_team6.repository.news;

import com.sprint.part3.sb01_monew_team6.entity.ArticleView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ArticleViewRepository extends JpaRepository<ArticleView,Long> {

  //articleId 로 ArticleView 수를 조회
  long countByArticleId(Long articleId);

  //특정 사용자가 특정 기사를 이미 조회했는지 여부를 반환.
  boolean existsByArticleIdAndUserId(Long articleId, Long userId);

  //특정 사용자가 특정 기사를 조회한 ArticleView 엔티티를 Optional로 반환.
  //중복 조회 시 timestamp 갱신을 위해 사용.
  Optional<ArticleView> findByArticleIdAndUserId(Long articleId, Long userId);
}


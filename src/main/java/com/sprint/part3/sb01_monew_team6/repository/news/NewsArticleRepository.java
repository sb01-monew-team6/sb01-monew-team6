package com.sprint.part3.sb01_monew_team6.repository.news;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface NewsArticleRepository extends JpaRepository<NewsArticle,Long>,NewsArticleRepositoryCustom {
  boolean existsBySourceUrl(String sourceUrl);
  List<NewsArticle> findAllByCreatedAtBetween(Instant from, Instant to);
}

package com.sprint.part3.sb01_monew_team6.repository.news;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsArticleRepository extends JpaRepository<NewsArticle,Long>,NewsArticleRepositoryCustom {
  boolean existsBySourceUrl(String sourceUrl);
}

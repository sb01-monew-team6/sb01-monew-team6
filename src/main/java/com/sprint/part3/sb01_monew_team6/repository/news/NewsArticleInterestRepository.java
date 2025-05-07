package com.sprint.part3.sb01_monew_team6.repository.news;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticleInterest;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsArticleInterestRepository extends JpaRepository<NewsArticleInterest,Long> {

}

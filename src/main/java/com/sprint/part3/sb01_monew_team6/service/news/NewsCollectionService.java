package com.sprint.part3.sb01_monew_team6.service.news;

import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import java.util.List;
import java.util.Optional;

public interface NewsCollectionService {
  Optional<List<NewsArticle>> collectAndSave();
}

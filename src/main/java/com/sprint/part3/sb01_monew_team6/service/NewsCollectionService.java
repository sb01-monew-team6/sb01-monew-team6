package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsCollectionService {
  private final NaverNewsClient naver;
  private final List<RssNewsClient> rssClients;
  private final NewsArticleRepository newsArticleRepository;
  private final InterestRepository interestRepository;

  @Transactional
  public void collectAndSave() {
    List<Interest> interests = interestRepository.findAll();

    // 관심사가 없으면 바로 종료
    if (interests.isEmpty()) {
      return;
    }

    List<ExternalNewsItem> externalNewsItems = fetchExternalNews(interests);
    List<NewsArticle> toSave = filterAndPrepareNewsArticles(externalNewsItems, interests);

    if (!toSave.isEmpty()) {
      newsArticleRepository.saveAll(toSave);
    }
  }

  private List<ExternalNewsItem> fetchExternalNews(List<Interest> interests) {
    List<ExternalNewsItem> items = new ArrayList<>();

    // 네이버 뉴스 페칭
    for (Interest interest : interests) {
      for (String keyword : interest.getKeyword()) {
        items.addAll(naver.fetchNews(keyword));
      }
    }

    // RSS 뉴스 페칭
    for (RssNewsClient rssClient : rssClients) {
      items.addAll(rssClient.fetchNews());
    }

    return items;
  }

  private List<NewsArticle> filterAndPrepareNewsArticles(List<ExternalNewsItem> items, List<Interest> interests) {
    List<NewsArticle> toSave = new ArrayList<>();
    Set<String> seenUrls = new HashSet<>(); // 이미 처리한 URL 기록

    for (ExternalNewsItem item : items) {
      String url = item.originalLink();
      if (isUniqueAndRelevant(item, url, seenUrls, interests)) {
        toSave.add(NewsArticle.from(item));
      }
    }

    return toSave;
  }

  // 처음 등장 URL, DB x, 제목에 관심사 키워드
  private boolean isUniqueAndRelevant(ExternalNewsItem item, String url, Set<String> seenUrls, List<Interest> interests) {
    return seenUrls.add(url)
        && !newsArticleRepository.existsBySourceUrl(url)
        && containsKeyword(item, interests);
  }

  // 제목에 관심사 키워드가 포함되어 있는지 확인
  private boolean containsKeyword(ExternalNewsItem item, List<Interest> interests) {
    return interests.stream()
        .anyMatch(interest -> interest.getKeyword().stream()
            .anyMatch(keyword -> item.title().contains(keyword)));
  }
}

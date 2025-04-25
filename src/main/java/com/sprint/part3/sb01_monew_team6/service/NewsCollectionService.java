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
    // 외부 뉴스 수집
    List<ExternalNewsItem> externalNewsItems = fetchExternalNews(interests);
    // 필터링,엔티티 변환
    List<NewsArticle> toSave = filterAndPrepareNewsArticles(externalNewsItems, interests);

    //저장
    if (!toSave.isEmpty()) {
      newsArticleRepository.saveAll(toSave);
    }
  }

  //Batch Chunk - ItemReader
  //관심사 기준으로 외부 뉴스 수집
  public List<ExternalNewsItem> fetchCandidates(){
    List<Interest> interests = interestRepository.findAll();
    //관심사 x
    if(interests.isEmpty()){
      return List.of();
    }
    //관심사 o
    return fetchExternalNews(interests);
  }

  //Batch Chunk - ItemWrtier
  //NewsArticle 목록을 받아 DB에 중복 없이 저장
  public void saveAll(List<NewsArticle> articles){
    List<NewsArticle> batchArticles = new ArrayList<>();
    for(NewsArticle article:articles){
      if(!newsArticleRepository.existsBySourceUrl(article.getSourceUrl())){
        batchArticles.add(article);
      }
    }
    if(!batchArticles.isEmpty()){
      newsArticleRepository.saveAll(batchArticles);
    }
  }

  // 관심사 키워드 기반으로 Naver + RSS 에서 뉴스 아이템 수집
  private List<ExternalNewsItem> fetchExternalNews(List<Interest> interests) {
    List<ExternalNewsItem> items = new ArrayList<>();

    // 네이버 뉴스
    for (Interest interest : interests) {
      for (String keyword : interest.getKeyword()) {
        items.addAll(naver.fetchNews(keyword));
      }
    }

    // RSS 뉴스
    for (RssNewsClient rssClient : rssClients) {
      items.addAll(rssClient.fetchNews());
    }

    return items;
  }

  //
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

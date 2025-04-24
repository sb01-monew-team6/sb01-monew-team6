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
  public void collectAndSave(){
    List<Interest> interests = interestRepository.findAll();
    List<ExternalNewsItem> items = new ArrayList<>();

    // 관심사가 없으면 바로 종료
    if (interests.isEmpty()) {
      return;
    }

    for(Interest i: interests){
      for(String keyword: i.getKeyword()){
        items.addAll(naver.fetchNews(keyword));
      }
    }
    for(RssNewsClient rss : rssClients){
      items.addAll(rss.fetchNews());
    }

    // 3) 중복 URL 제거·필터링해서 저장 대기 리스트 생성
    List<NewsArticle> toSave = new ArrayList<>();
    Set<String> seenUrls = new HashSet<>();  // 이미 처리한 URL 기록

    for (ExternalNewsItem e : items) {
      String url = e.originalLink();
      // ① 최초 등장한 URL이고
      // ② DB에도 없고
      // ③ 제목에 관심사 키워드가 포함되어 있으면
      if (seenUrls.add(url)
          && !newsArticleRepository.existsBySourceUrl(url)
          && interests.stream().anyMatch(
          interest -> interest.getKeyword().stream()
              .anyMatch(keyword -> e.title().contains(keyword))
      )) {
        toSave.add(NewsArticle.from(e));
      }
    }

    if(!toSave.isEmpty()){
      newsArticleRepository.saveAll(toSave);
    }
  }
}

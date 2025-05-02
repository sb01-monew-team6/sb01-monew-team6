package com.sprint.part3.sb01_monew_team6.service.news.impl;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsCollectionImplService {
  private final NaverNewsClient naver;
  private final List<RssNewsClient> rssClients;
  private final NewsArticleRepository newsArticleRepository;
  private final InterestRepository interestRepository;

  @Transactional
  public void collectAndSave() {
    log.info("뉴스 수집 시작");

    List<Interest> interests = interestRepository.findAll();
    log.debug("조회된 관심사 개수: {}", interests.size());

    // 관심사 x
    if (interests.isEmpty() || interests.get(0).getKeywords() == null || interests.get(0).getKeywords().isEmpty()) {
      log.info("등록된 관심사가 없어 뉴스 수집을 건너뜁니다");
      return;   // 예외 대신 조용히 리턴
    }
    // 외부 뉴스 수집
    List<ExternalNewsItem> externalNewsItems = fetchExternalNews(interests);
    log.debug("수집된 외부 뉴스 아이템 개수: {}", externalNewsItems.size());

    // 필터링,엔티티 변환
    List<NewsArticle> toSave = filterAndPrepareNewsArticles(externalNewsItems, interests);
    log.debug("저장 준비된 NewsArticle 개수: {}", toSave.size());

    //저장
    if (toSave.isEmpty()) {
      log.info("저장할 뉴스가 없어 작업을 종료합니다");
      return;   // 예외 대신 조용히 리턴
    }

    newsArticleRepository.saveAll(toSave);
    log.info("뉴스 {}건 저장 완료", toSave.size());
  }

  //Batch Chunk - ItemReader
  //관심사 기준으로 외부 뉴스 수집
  public List<ExternalNewsItem> fetchCandidates(){
    log.info("Batch용 뉴스 후보 수집 시작");

    List<Interest> interests = interestRepository.findAll();
    log.debug("조회된 관심사 개수: {}", interests.size());

    //관심사 x
    if(interests.isEmpty() || interests.get(0).getKeywords() == null || interests.get(0).getKeywords().isEmpty()){
      log.info("Batch용 등록된 관심사가 없어 빈 리스트 반환");
      return List.of();   // 예외 대신 빈 리스트
    }
    //관심사 o
    List<ExternalNewsItem> items = fetchExternalNews(interests);
    log.info("Batch용 수집 완료: {}개 아이템", items.size());
    return items;
  }

  //Batch Chunk - ItemWrtier
  //NewsArticle 목록을 받아 DB에 중복 없이 저장
  public void saveAll(List<NewsArticle> articles){
    log.info("Batch용 뉴스 저장 시작: 입력 {}건", articles.size());

    List<NewsArticle> batchArticles = new ArrayList<>();

    for(NewsArticle article:articles){
      if(!newsArticleRepository.existsBySourceUrl(article.getSourceUrl())){
        batchArticles.add(article);
      }
    }

    log.debug("Batch용 저장 대상 필터링 후: {}건", batchArticles.size());

    if(batchArticles.isEmpty()){
      log.info("Batch 저장할 뉴스가 없습니다");
      throw new NewsException(ErrorCode.NEWS_BATCH_NO_NEWS_EXCEPTION,Instant.now(),HttpStatus.NOT_FOUND);
    }

    newsArticleRepository.saveAll(batchArticles);
    log.info("Batch용 뉴스 {}건 저장 완료", batchArticles.size());
  }

  // 관심사 키워드 기반으로 Naver + RSS 에서 뉴스 아이템 수집
  private List<ExternalNewsItem> fetchExternalNews(List<Interest> interests) {
    List<ExternalNewsItem> items = new ArrayList<>();

    // 네이버 뉴스
    for (Interest interest : interests) {
      for (String keyword : interest.getKeywords()) {
        try {
          List<ExternalNewsItem> newsItems = naver.fetchNews(keyword);
          log.debug("네이버에서 '{}' 키워드로 {}건 수집", keyword, newsItems.size());
          items.addAll(newsItems);
        } catch (Exception e) {
          log.error("네이버 API 호출 실패 : 키워드='{}'", keyword, e);
          throw new NewsException(ErrorCode.NEWS_NAVERCLIENT_EXCEPTION, Instant.now(),
              HttpStatus.BAD_GATEWAY);
        }
      }

      // RSS 뉴스
      for (RssNewsClient rssClient : rssClients) {
        try {
          List<ExternalNewsItem> newsItems = rssClient.fetchNews();
          log.debug("RSS 클라이언트 '{}'에서 {}건 수집", rssClient.getClass().getSimpleName(),
              newsItems.size());
          items.addAll(newsItems);
        } catch (Exception e) {
          log.error("RSS API 호출 실패", e);
          throw new NewsException(ErrorCode.NEWS_RSSCLIENT_EXCEPTION, Instant.now(),
              HttpStatus.BAD_GATEWAY);
        }
      }
    }
    return items;
  }

  private List<NewsArticle> filterAndPrepareNewsArticles (List < ExternalNewsItem > items, List < Interest > interests){
      List<NewsArticle> toSave = new ArrayList<>();
      Set<String> seenUrls = new HashSet<>(); // 이미 처리한 URL 기록

      for (ExternalNewsItem item : items) {
        String url = item.originalLink();
        if (seenUrls.add(url) && !newsArticleRepository.existsBySourceUrl(url) && containsKeyword(
            item, interests)) {
          toSave.add(NewsArticle.from(item));
        }
      }

      return toSave;
    }

  // 제목 또는 설명에 관심사 키워드가 포함되어 있는지 확인
  private boolean containsKeyword(ExternalNewsItem item, List<Interest> interests) {
    return interests.stream()
          .anyMatch(interest ->
              interest.getKeywords().stream()
                  .anyMatch(keyword -> item.title().contains(keyword)|| item.description().contains(keyword))
          );
  }
}

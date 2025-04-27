package com.sprint.part3.sb01_monew_team6.client.impl;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NaverNewsClientImpl implements NaverNewsClient {
  private final WebClient webClient;
  private final String clientId,clientSecret;

  public NaverNewsClientImpl(WebClient.Builder builder,
      @Value("${naver.api.url}") String apiUrl,
      @Value("${naver.api.client-id}") String clientId,
      @Value("${naver.api.client-secret}") String clientSecret){
    this.webClient = builder.baseUrl(apiUrl).build();
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Override
  public List<ExternalNewsItem> fetchNews(String keyword) {
    //api 호출
    NaverResponse response = webClient.get()
        .uri(u->u.queryParam("query",keyword).build())
        .header("X-Naver-Client-Id",clientId)
        .header("X-Naver-Client-Secret",clientSecret)
        .retrieve()
        .bodyToMono(NaverResponse.class)
        .block();

    DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    //빈 응답 처리
    if(response == null || response.items == null){
      return List.of();
    }

    //DTO -> Entity
    DateTimeFormatter rfc822 = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    return response.items.stream()
        .map(item -> {
          // "Sun, 27 Apr 2025 22:30:00 +0900" → Instant
          Instant published = OffsetDateTime
              .parse(item.pubDate, rfc822)
              .toInstant();

          return new ExternalNewsItem(
              "NAVER",
              item.originallink,
              item.link,
              item.title,
              published,
              item.description
          );
        })
        .toList();
  }

  //내부 응답 매핑용 클래스
  public static class NaverResponse {
    public List<Item> items;
    public static class Item {
      public String originallink;
      public String title;
      public String link;
      public String description;
      public String pubDate;
    }
  }
}

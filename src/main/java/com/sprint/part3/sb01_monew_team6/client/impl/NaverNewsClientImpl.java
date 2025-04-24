package com.sprint.part3.sb01_monew_team6.client.impl;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.time.ZonedDateTime;
import java.util.List;
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

    //빈 응답 처리
    if(response == null || response.items == null){
      return List.of();
    }

    //DTO -> Entity
    return response.items.stream()
        .map(item -> new ExternalNewsItem(
            "Naver",
            item.originalLink,
            item.link,
            item.title,
            item.pubDate,
            item.description
        ))
        .toList();
  }

  //내부 응답 매핑용 클래스
  public static class NaverResponse {
    public List<Item> items;
    public static class Item {
      public String originalLink;
      public String title;
      public String link;
      public String description;
      public ZonedDateTime pubDate;
    }
  }
}

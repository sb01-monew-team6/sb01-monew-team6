package com.sprint.part3.sb01_monew_team6.client.impl;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HankyungRssClientImpl implements RssNewsClient {

  private static final String FEED_URL = "https://www.hankyung.com/feed/all-news";

  @Override
  public List<ExternalNewsItem> fetchNews() {
    try (InputStream in = createInputStream();
        XmlReader reader = createReader(in)) {

      //SyndFeed 는 저자, 기여자, 저작권, 모듈, 게시 날짜, 이미지, 외국 마크업 및 언어를 포함하여 더 많은 필드를 추가할 수 있는 기회를 제공
      //ROME 라이브러리의 SyndFeedInput을 사용해 XML을 SyndFeed 객체로 변환
      SyndFeed feed = new SyndFeedInput().build(reader);
      return feed.getEntries().stream()
          .map(e -> new ExternalNewsItem(
              "RSS",
              e.getLink(),
              e.getLink(),
              e.getTitle(),
              Optional.ofNullable(e.getPublishedDate())
                  .map(Date::toInstant)
                  .orElse(Instant.now()),
              e.getDescription() != null
                  ? e.getDescription().getValue()
                  : ""
          ))
          .collect(Collectors.toList());

    } catch (FeedException | IOException ex) {
      log.error("RSS API 호출 실패", ex);
      throw new NewsException(
          ErrorCode.NEWS_RSSCLIENT_EXCEPTION,
          Instant.now(),
          HttpStatus.BAD_GATEWAY
      );
    }
  }

  /**
   * HTTP 대신 테스트용으로 교체할 수 있게 분리된 스트림 생성 메서드
   */
  protected InputStream createInputStream() throws IOException {
    return new URL(FEED_URL).openStream();
  }

  /**
   * JDOM XmlReader 생성 분리 (필요하면 테스트에서 오버라이드)
   */
  protected XmlReader createReader(InputStream in) throws IOException {
    return new XmlReader(in);
  }
}
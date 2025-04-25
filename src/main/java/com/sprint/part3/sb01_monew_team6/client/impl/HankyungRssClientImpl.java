package com.sprint.part3.sb01_monew_team6.client.impl;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.net.URL;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class HankyungRssClientImpl implements RssNewsClient {

  private static final String H_URL = "https://www.hankyun.com/feed";

  @Override
  public List<ExternalNewsItem> fetchNews() {
    try(XmlReader reader = new XmlReader(new URL(H_URL))){
      SyndFeed feed = readFeed(reader);
      return feed.getEntries().stream()
          .map(e->new ExternalNewsItem(
              "HANKYUNG",
              e.getLink(),
              e.getLink(),
              e.getTitle(),
              e.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()),
              e.getDescription().getValue()
              ))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  // 테스트용으로 분리한 메서드(DOCTYPE 허용하도록 setAllowDoctypes(true) 추가)
  protected SyndFeed readFeed(XmlReader reader) throws Exception, FeedException {
    SyndFeedInput input = new SyndFeedInput();
    input.setAllowDoctypes(true);
    return input.build(reader);
  }
}

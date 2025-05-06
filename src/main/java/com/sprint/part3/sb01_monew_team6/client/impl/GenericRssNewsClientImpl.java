package com.sprint.part3.sb01_monew_team6.client.impl;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.client.RssProperties;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenericRssNewsClientImpl implements RssNewsClient {
  private final RssProperties props;

  @Override
  public List<ExternalNewsItem> fetchNews() {
    List<ExternalNewsItem> all = new ArrayList<>();
    for (RssProperties.Feed feed : props.getFeeds()) {
      try (InputStream in = new URL(feed.getUrl()).openStream();
          XmlReader reader = new XmlReader(in)) {
        SyndFeed synd = new SyndFeedInput().build(reader);
        List<ExternalNewsItem> items = synd.getEntries().stream()
            .map(e -> new ExternalNewsItem(
                feed.getName(),
                e.getLink(), e.getLink(),
                e.getTitle(),
                Optional.ofNullable(e.getPublishedDate())
                    .map(Date::toInstant).orElse(Instant.now()),
                e.getDescription()!=null?e.getDescription().getValue():""
            )).toList();
        log.debug("{} RSS {}건 반환", feed.getName(), items.size());
        all.addAll(items);
      } catch (Exception ex) {
        log.error("RSS 호출 실패: {}", feed.getName(), ex);
      }
    }
    return all;
  }
}
package com.sprint.part3.sb01_monew_team6.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sprint.part3.sb01_monew_team6.client.impl.HankyungRssClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class HankyungRssClientTest {

  @Test
  @DisplayName("RSS XML → When fetchNews → Then 매핑된 ExternalNewsItem 반환")
  void rssXML_whenFetchNews_thenMapped() throws IOException, FeedException {
    //given
    String xml = "<rss><channel>"
        + "<item><title>T</title><link>http://l</link>"
        + "<pubDate>Wed, 23 Apr 2025 10:00:00 +0000</pubDate>"
        + "<description>D</description></item>"
        + "</channel></rss>";

    SyndFeed feed = new SyndFeedInput().build(
        new XmlReader(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        )
    );

    //when
    HankyungRssClient client = new HankyungRssClient() {
      @Override
      protected SyndFeed readFeed(XmlReader reader) { return feed; }
    };
    List<ExternalNewsItem> list = client.fetchNews();

    //then
    assertThat(list).hasSize(1)
        .extracting(ExternalNewsItem::title)
        .containsExactly("T");
  }
}

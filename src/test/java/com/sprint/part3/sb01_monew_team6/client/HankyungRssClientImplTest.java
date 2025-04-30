package com.sprint.part3.sb01_monew_team6.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rometools.rome.io.FeedException;
import com.sprint.part3.sb01_monew_team6.client.impl.HankyungRssClientImpl;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HankyungRssClientImplTest {

  @Test
  @DisplayName("빈 RSS 채널이면 빈 리스트 반환")
  void fetchNews_emptyChannel_thenEmpty() throws Exception {
    HankyungRssClientImpl client = new HankyungRssClientImpl() {
      @Override
      protected InputStream createInputStream() {
        String xml = "<rss><channel></channel></rss>";
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
      }
    };

    List<ExternalNewsItem> list = client.fetchNews();
    assertThat(list).isEmpty();
  }

  @Test
  @DisplayName("여러 <item>이 있으면 모두 매핑")
  void fetchNews_multipleItems_mappedCorrectly() throws Exception {
    String xml =
        "<rss><channel>"
            + "  <item>"
            + "    <title>First</title>"
            + "    <link>http://a</link>"
            + "    <pubDate>Wed, 23 Apr 2025 10:00:00 +0000</pubDate>"
            + "    <description>D1</description>"
            + "  </item>"
            + "  <item>"
            + "    <title>Second</title>"
            + "    <link>http://b</link>"
            + "    <pubDate>Thu, 24 Apr 2025 11:00:00 +0000</pubDate>"
            + "    <description>D2</description>"
            + "  </item>"
            + "</channel></rss>";

    HankyungRssClientImpl client = new HankyungRssClientImpl() {
      @Override
      protected InputStream createInputStream() {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
      }
    };

    List<ExternalNewsItem> list = client.fetchNews();
    assertThat(list)
        .hasSize(2)
        .extracting(ExternalNewsItem::title)
        .containsExactly("First", "Second");
  }
  @Test
  @DisplayName("유효하지 않은 XML이면 Exception 발생")
  void fetchNews_invalidXml_throwsException() {
    HankyungRssClientImpl client = new HankyungRssClientImpl() {
      @Override
      protected InputStream createInputStream() {
        return new ByteArrayInputStream("not xml".getBytes(StandardCharsets.UTF_8));
      }
    };

    assertThatThrownBy(client::fetchNews)
        .isInstanceOf(NewsException.class);
  }

  @Test
  @DisplayName("createInputStream에서 IOException이면 fetchNews도 IOException")
  void fetchNews_ioExceptionFromCreateInputStream() {
    HankyungRssClientImpl client = new HankyungRssClientImpl() {
      @Override
      protected InputStream createInputStream() throws IOException {
        throw new IOException("connection failed");
      }
    };

    assertThatThrownBy(client::fetchNews)
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("RSS API 요청 오류입니다");
  }

  @Test
  @DisplayName("RSS XML → When fetchNews → Then 매핑된 ExternalNewsItem 반환")
  void rssXML_whenFetchNews_thenMapped() throws IOException, FeedException {
    // given
    String xml = "<rss><channel>"
        + "<item><title>T</title><link>http://l</link>"
        + "<pubDate>Wed, 23 Apr 2025 10:00:00 +0000</pubDate>"
        + "<description>D</description></item>"
        + "</channel></rss>";

    // when: createInputStream() 만 오버라이드
    HankyungRssClientImpl client = new HankyungRssClientImpl() {
      @Override
      protected InputStream createInputStream() {
        // 네트워크 대신 메모리 스트림 주입
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
      }
    };

    List<ExternalNewsItem> list = client.fetchNews();

    // then
    assertThat(list)
        .hasSize(1)
        .extracting(ExternalNewsItem::title)
        .containsExactly("T");
  }
}
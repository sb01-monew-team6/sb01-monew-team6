package com.sprint.part3.sb01_monew_team6.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.part3.sb01_monew_team6.client.impl.GenericRssNewsClientImpl;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GenericRssNewsClientImplTest {
  @TempDir
  Path tempDir;

  private RssProperties props;
  private GenericRssNewsClientImpl client;

  @BeforeEach
  void setUp() {
    props = new RssProperties();
    client = new GenericRssNewsClientImpl(props);
  }

  @Test
  @DisplayName("Atom 피드를 정상 파싱해 ExternalNewsItem 리스트로 반환한다")
  void fetchNews_parsesAtomFeed(@TempDir Path tempDir) throws Exception {
    // 샘플 Atom 피드 XML
    String xml = """
      <?xml version="1.0" encoding="UTF-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">
        <title>Test Feed</title>
        <entry>
          <title>Title1</title>
          <link href="http://example.com/1"/>
          <published>2025-01-01T00:00:00Z</published>
          <id>1</id>
          <summary>Desc1</summary>
        </entry>
        <entry>
          <title>Title2</title>
          <link href="http://example.com/2"/>
          <!-- published 누락 시 Instant.now() 대체 -->
          <id>2</id>
          <summary>Desc2</summary>
        </entry>
      </feed>
      """;

    // 임시 파일에 XML 기록
    Path feedFile = tempDir.resolve("feed.xml");
    Files.writeString(feedFile, xml);

    // RssProperties 설정
    RssProperties.Feed f = new RssProperties.Feed();
    f.setName("TEST");
    f.setUrl(feedFile.toUri().toString());
    props.setFeeds(List.of(f));

    // 실행
    List<ExternalNewsItem> items = client.fetchNews();

    // 검증
    assertThat(items).hasSize(2);
    ExternalNewsItem first = items.get(0);
    assertThat(first.provider()).isEqualTo("TEST");
    assertThat(first.link()).isEqualTo("http://example.com/1");
    assertThat(first.originalLink()).isEqualTo("http://example.com/1");
    assertThat(first.title()).isEqualTo("Title1");
    assertThat(first.description()).isEqualTo("Desc1");
    assertThat(first.pubDate()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));

    ExternalNewsItem second = items.get(1);
    assertThat(second.provider()).isEqualTo("TEST");
    assertThat(second.link()).isEqualTo("http://example.com/2");
    assertThat(second.title()).isEqualTo("Title2");
    assertThat(second.description()).isEqualTo("Desc2");
    // published 누락된 경우, pubDate가 현재 시각 수준이라는 것만 확인
    assertThat(second.pubDate()).isNotNull();
  }

  @Test
  @DisplayName("피드 URL이 잘못되면 예외 없이 빈 리스트를 반환한다")
  void fetchNews_withBadUrl_returnsEmpty() {
    RssProperties.Feed bad = new RssProperties.Feed();
    bad.setName("BAD");
    bad.setUrl("file:///nonexistent/doesnotexist.xml");
    props.setFeeds(List.of(bad));

    List<ExternalNewsItem> items = client.fetchNews();
    assertThat(items).isEmpty();
  }

  @Test
  @DisplayName("여러 피드를 설정하면 순서대로 모두 합쳐 반환한다")
  void fetchNews_multipleFeeds_concatAll() throws Exception {
    // 첫 번째 피드
    String xml1 = """
      <feed xmlns="http://www.w3.org/2005/Atom"><entry>
        <title>A</title><link href="http://a"/><published>2025-02-02T02:02:02Z</published><id>1</id><summary>DA</summary>
      </entry></feed>
      """;
    Path file1 = tempDir.resolve("f1.xml");
    Files.writeString(file1, xml1);
    RssProperties.Feed feed1 = new RssProperties.Feed();
    feed1.setName("F1");
    feed1.setUrl(file1.toUri().toString());

    // 두 번째 피드
    String xml2 = """
      <feed xmlns="http://www.w3.org/2005/Atom"><entry>
        <title>B</title><link href="http://b"/><published>2025-03-03T03:03:03Z</published><id>2</id><summary>DB</summary>
      </entry></feed>
      """;
    Path file2 = tempDir.resolve("f2.xml");
    Files.writeString(file2, xml2);
    RssProperties.Feed feed2 = new RssProperties.Feed();
    feed2.setName("F2");
    feed2.setUrl(file2.toUri().toString());

    props.setFeeds(List.of(feed1, feed2));

    List<ExternalNewsItem> items = client.fetchNews();
    assertThat(items).hasSize(2)
        .extracting(ExternalNewsItem::title)
        .containsExactly("A", "B");
  }
}

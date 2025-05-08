package com.sprint.part3.sb01_monew_team6.client;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rss")
public class RssProperties {
  private List<Feed> feeds;
  @Data
  public static class Feed {
    private String name;
    private String url;
  }
}
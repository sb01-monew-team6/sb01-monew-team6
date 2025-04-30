package com.sprint.part3.sb01_monew_team6.client;

import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.util.List;

public interface RssNewsClient {
  List<ExternalNewsItem> fetchNews();
}

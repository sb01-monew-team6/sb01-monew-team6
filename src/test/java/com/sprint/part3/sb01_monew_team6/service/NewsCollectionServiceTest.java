package com.sprint.part3.sb01_monew_team6.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NewsCollectionServiceTest {
  @Mock
  NaverNewsClient naverClient;
  @Mock
  RssNewsClient rssClient;
  @Mock
  InterestRepository interestRepository;
  @Mock
  NewsArticleRepository newsArticleRepository;

  private NewsCollectionService service;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);                   
    service = new NewsCollectionService(naverClient, List.of(rssClient), newsArticleRepository, interestRepository);
  }

  @Test
  @DisplayName("키워드 포함 기사만 저장")
  void save_News_Only_With_Keyword() {
    //given
    Interest i = new Interest();
    i.setName("스포츠");
    i.setKeyword(List.of("축구","야구"));

    given(interestRepository.findAll()).willReturn(List.of(i));

    ExternalNewsItem e1 = new ExternalNewsItem(
        "Naver","url1","url1","축구 제목", ZonedDateTime.now(),""
    );

    given(naverClient.fetchNews("축구")).willReturn(List.of(e1));
    given(rssClient.fetchNews()).willReturn(List.of());
    given(newsArticleRepository.existsBySourceUrl("url1")).willReturn(false);

    //when
    service.collectAndSave();

    // then: Iterable 크기 검사
    then(newsArticleRepository).should().saveAll(argThat(iter -> {
      int cnt = 0;
      for (var x : iter) cnt++;
      return cnt == 1;
    }));
  }
}

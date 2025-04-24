package com.sprint.part3.sb01_monew_team6.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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

  @Test
  @DisplayName("중복 URL 하나만 저장")
  void duplicatedUrl_save_oneUrl() {
    //given
    Interest i = new Interest();
    i.setName("스포츠");
    i.setKeyword(List.of("축구", "야구"));
    given(interestRepository.findAll()).willReturn(List.of(i));

    ExternalNewsItem e1 = new ExternalNewsItem(
        "Naver", "url1", "url1", "축구제목", ZonedDateTime.now(), "요약"
    );
    given(naverClient.fetchNews("축구")).willReturn(List.of(e1, e1));
    given(rssClient.fetchNews()).willReturn(List.of());
    given(newsArticleRepository.existsBySourceUrl("url1")).willReturn(false);

    // when
    service.collectAndSave();

    // then
    then(newsArticleRepository).should().saveAll(argThat(iter ->
        ((java.util.Collection<?>) iter).size() == 1
    ));
  }

  @Test
  @DisplayName("이미 DB에 있는 기사만 나왔을 땐, 레포지토리의 저장 메서드를 아예 호출하지 않음")
  void allExisting_thenNoSave() {
    // given
    Interest it = new Interest();
    it.setKeyword(List.of("x"));
    given(interestRepository.findAll()).willReturn(List.of(it));
    ExternalNewsItem e = new ExternalNewsItem("NAVER","x","x","x",ZonedDateTime.now(),"");
    given(naverClient.fetchNews("x")).willReturn(List.of(e));
    given(rssClient.fetchNews()).willReturn(List.of());
    given(newsArticleRepository.existsBySourceUrl("x")).willReturn(true);

    // when
    service.collectAndSave();

    // then
    then(newsArticleRepository).should(never()).saveAll(any());
  }

  
}

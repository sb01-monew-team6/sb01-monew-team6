package com.sprint.part3.sb01_monew_team6.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class NewsCollectionServiceTest {
  @Mock NaverNewsClient naverClient;
  @Mock RssNewsClient   rssClient;
  @Mock InterestRepository interestRepository;
  @Mock NewsArticleRepository newsArticleRepository;

  private NewsCollectionService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    // 단일 목을 실제 리스트로 묶어서 서비스에 주입
    service = new NewsCollectionService(
        naverClient,
        List.of(rssClient),
        newsArticleRepository,
        interestRepository
    );
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
        "Naver","url1","url1","축구 제목", Instant.now(),""
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
  @DisplayName("제목에는 없고 요약(설명)에 키워드가 포함될 시 저장")
  void save_News_With_Keyword_In_Description() {
    //given
    Interest i = new Interest();
    i.setKeyword(List.of("축구","야구","농구"));
    given(interestRepository.findAll()).willReturn(List.of(i));

    ExternalNewsItem e1 = new ExternalNewsItem(
        "Naver","u1","u1","title",Instant.now(),"description 축구 "
    );
    given(naverClient.fetchNews("축구")).willReturn(List.of(e1));
    given(rssClient.fetchNews()).willReturn(List.of());
    given(newsArticleRepository.existsBySourceUrl("u1")).willReturn(false);

    //when
    service.collectAndSave();

    //then
    then(newsArticleRepository).should().saveAll(
        argThat(iter -> {
          int cnt = 0;
          for (var x : iter) cnt++;
          return cnt == 1;
        })
    );

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
        "Naver", "url1", "url1", "축구제목", Instant.now(), "요약"
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
    ExternalNewsItem e = new ExternalNewsItem("NAVER","x","x","x",Instant.now(),"");
    given(naverClient.fetchNews("x")).willReturn(List.of(e));
    given(rssClient.fetchNews()).willReturn(List.of());
    given(newsArticleRepository.existsBySourceUrl("x")).willReturn(true);

    // when
    service.collectAndSave();

    // then
    then(newsArticleRepository).should(never()).saveAll(any());
  }

  @Test
  @DisplayName("관심사 없으면 외부 호출·저장 모두 안 함")
  void givenNoInterests_thenNothing() {
    // Given
    given(interestRepository.findAll()).willReturn(List.of());

    // When
    service.collectAndSave();

    // Then
    then(naverClient).should(never()).fetchNews(any());
    then(rssClient).should(never()).fetchNews();
    then(newsArticleRepository).should(never()).saveAll(any());
  }

  @Test
  @DisplayName("NaverClient 예외 발생 - collectAndSave in Service")
  void exception_NaverClient_in_collectAndSave() {
    //given
    Interest i = new Interest();
    i.setKeyword(List.of("x"));
    given(interestRepository.findAll()).willReturn(List.of(i));
    given(naverClient.fetchNews("x")).willThrow(new NewsException(ErrorCode.NEWS_NAVERCLIENT_EXCEPTION,Instant.now(),
        HttpStatus.BAD_GATEWAY));

    //when,then
    assertThatThrownBy(()->service.collectAndSave())
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("NAVER API 요청 오류입니다.");
  }
  @Test
  @DisplayName("RssClient 예외 발생 - collectAndSave in Service")
  void excpetion_RssClient_in_collectAndSave(){
    //given
    Interest i = new Interest();
    i.setKeyword(List.of("x"));
    given(interestRepository.findAll()).willReturn(List.of(i));
    given(rssClient.fetchNews()).willThrow(new NewsException(ErrorCode.NEWS_RSSCLIENT_EXCEPTION,Instant.now(),HttpStatus.BAD_GATEWAY));
    //when,then
    assertThatThrownBy(()->service.collectAndSave())
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("RSS API 요청 오류입니다.");
  }
}

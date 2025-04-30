package com.sprint.part3.sb01_monew_team6.service.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.sprint.part3.sb01_monew_team6.client.NaverNewsClient;
import com.sprint.part3.sb01_monew_team6.client.RssNewsClient;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.impl.NewsCollectionImplService;
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
public class NewsCollectionImplServiceTest {
  @Mock NaverNewsClient naverClient;
  @Mock RssNewsClient   rssClient;
  @Mock InterestRepository interestRepository;
  @Mock NewsArticleRepository newsArticleRepository;

  private NewsCollectionImplService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    // 단일 목을 실제 리스트로 묶어서 서비스에 주입
    service = new NewsCollectionImplService(
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
    Interest i = Interest.builder()
        .name("스포츠")
        .keywords(List.of("축구","야구"))
        .build();

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
    Interest i = Interest.builder()
        .name("k")
        .keywords(List.of("축구","야구","농구"))
        .build();
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
    Interest i = Interest.builder()
        .name("스포츠")
        .keywords(List.of("축구", "야구"))
        .build();
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
  @DisplayName("이미 DB에 있는 기사만 나왔을 땐, 예외 없이 종료하고 저장 안 함")
  void allExisting_throwNoNewsException() {
    // given
    Interest it = Interest.builder()
        .name("k")
        .keywords(List.of("x"))
        .build();
    given(interestRepository.findAll()).willReturn(List.of(it));
    ExternalNewsItem e = new ExternalNewsItem("NAVER","x","x","x",Instant.now(),"");
    given(naverClient.fetchNews("x")).willReturn(List.of(e));
    given(rssClient.fetchNews()).willReturn(List.of());
    given(newsArticleRepository.existsBySourceUrl("x")).willReturn(true);

    // when - 예외 없이 실행
    service.collectAndSave();
    // then - saveAll 절대 호출 안 됨
    then(newsArticleRepository).should( times(0) ).saveAll(org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  @DisplayName("관심사 없으면 예외 없이 종료하고 저장 안 함")
  void givenNoInterests_throwNewsException() {
    // Given
    given(interestRepository.findAll()).willReturn(List.of());

    // when - 예외 없이 실행
    service.collectAndSave();
    //then - 외부 호출도 하지 않음
    then(naverClient).shouldHaveNoInteractions();
    then(rssClient).shouldHaveNoInteractions();
    then(newsArticleRepository).should( times(0) ).saveAll(org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  @DisplayName("NaverClient 예외 발생 - collectAndSave in Service")
  void exception_NaverClient_in_collectAndSave() {
    //given
    Interest i = Interest.builder()
        .name("k")
        .keywords(List.of("x"))
        .build();
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
    Interest i = Interest.builder()
        .name("k")
        .keywords(List.of("x"))
        .build();
    given(interestRepository.findAll()).willReturn(List.of(i));
    // 네이버는 빈 리스트 반환 → RSS 로직으로 넘어가게
    given(naverClient.fetchNews("x")).willReturn(List.of());
    // RSS 에서만 예외
    given(rssClient.fetchNews())
        .willThrow(new NewsException(
            ErrorCode.NEWS_RSSCLIENT_EXCEPTION,
            Instant.now(),
            HttpStatus.BAD_GATEWAY));

    // when & then
    assertThatThrownBy(() -> service.collectAndSave())
        .isInstanceOf(NewsException.class)
        .hasMessageContaining("RSS API 요청 오류입니다.");
  }

  //fetchCandidates()
  @Test
  @DisplayName("관심사가 있을 떄 naver,rss 호출 결과 반환")
  void whenInterests_returnsItems(){
    //given
    Interest i = Interest.builder()
        .name("k")
        .keywords(List.of("key"))
        .build();
    given(interestRepository.findAll()).willReturn(List.of(i));

    ExternalNewsItem e1 = new ExternalNewsItem("NAVER","u1","u1","key 제목", Instant.now(),"");
    ExternalNewsItem e2 = new ExternalNewsItem("RSS","u2","u2","제목", Instant.now(),"");
    given(naverClient.fetchNews("key")).willReturn(List.of(e1));
    given(rssClient.fetchNews()).willReturn(List.of(e2));

    // when
    List<ExternalNewsItem> result = service.fetchCandidates();

    // then
    assertThat(result).hasSize(2)
        .containsExactlyInAnyOrder(e1, e2);
  }

  @Test
  @DisplayName("관심사 없으면 빈리스트 반환")
  void retturnEmptyList_whenNoInterests() {
    // given
    given(interestRepository.findAll()).willReturn(List.of());

    // when
    List<ExternalNewsItem> result = service.fetchCandidates();
    // then
    assertThat(result).isEmpty();
    then(interestRepository).should(times(1)).findAll();
  }

  //saveAll()
  @Test
  @DisplayName("새로운 기사만 저장,호출")
  void newNews_save_call(){
    //given
    NewsArticle a1 = NewsArticle.from(
        new ExternalNewsItem("Naver","url1","url1","title1",Instant.now(),"desc1"));
    NewsArticle a2 = NewsArticle.from(
        new ExternalNewsItem("Rss","url2","url2","title2",Instant.now(),"desc2")
    );
    // u1은 DB에 없고, u2는 이미 존재
    given(newsArticleRepository.existsBySourceUrl("url1")).willReturn(false);
    given(newsArticleRepository.existsBySourceUrl("url2")).willReturn(true);
    //when
    service.saveAll(List.of(a1, a2));
    // then: a1만 담긴 리스트로 saveAll 호출
    then(newsArticleRepository).should().saveAll(argThat(iter -> {
      // Iterable 크기 검사
      int cnt = 0;
      for (var x : iter) cnt++;
      return cnt == 1;
    }));
  }
  @Test
  @DisplayName("저장 대상이 없으면 예외 발생")
  void noNews_throwsException(){
    //given
    NewsArticle a1 = NewsArticle.from(
        new ExternalNewsItem("Naver","url1","url1","title1",Instant.now(),"desc1"));
    NewsArticle a2 = NewsArticle.from(
        new ExternalNewsItem("Rss","url2","url2","title2",Instant.now(),"desc2")
    );
    given(newsArticleRepository.existsBySourceUrl("url1")).willReturn(true);
    given(newsArticleRepository.existsBySourceUrl("url2")).willReturn(true);

    // when & then
    assertThatThrownBy(() -> service.saveAll(List.of(a1, a2)) )
        .isInstanceOf(NewsException.class)
        .satisfies(ex->{
          NewsException ne = (NewsException) ex;
          assertThat(ne.getCode()).isEqualTo(ErrorCode.NEWS_BATCH_NO_NEWS_EXCEPTION);
        });
  }
}

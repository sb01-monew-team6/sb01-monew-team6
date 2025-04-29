package com.sprint.part3.sb01_monew_team6.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.sprint.part3.sb01_monew_team6.client.impl.NaverNewsClientImpl;
import com.sprint.part3.sb01_monew_team6.client.impl.NaverNewsClientImpl.NaverResponse;
import com.sprint.part3.sb01_monew_team6.dto.news.ExternalNewsItem;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class NaverNewsClientImplTest {

  //WebClient.Builder Mock
  @Mock WebClient.Builder builder;
  //WebClient Mock
  @Mock WebClient webClient;
  //Get 요청 URI 설정
  @SuppressWarnings("rawtypes")
  @Mock WebClient.RequestHeadersUriSpec uriSpec;
  //헤더,URI 등 설정된 후 요청 스펙
  @SuppressWarnings("rawtypes")
  @Mock WebClient.RequestHeadersSpec headersSpec;
  //응답 스펙
  @Mock WebClient.ResponseSpec responseSpec;

  //테스트 대상
  private NaverNewsClientImpl client;

  @BeforeEach
  void setUp(){
    //Mock 어노테이션 초기화
    MockitoAnnotations.openMocks(this);

    given(builder.baseUrl(anyString())).willReturn(builder); //builder.baseUrl(...) 호출 시 자기 자신 리턴
    given(builder.build()).willReturn(webClient); //  builder.build() 호출 시 webClient 리턴

    //실제 테스트 대상 생성
    client = new NaverNewsClientImpl(builder,"http://api","id","secret");

    //Get 호출 스텁 설정( 일관된 WebClient 호출 )
    given(webClient.get()).willReturn(uriSpec);
    given(uriSpec.uri(any(Function.class))).willReturn(headersSpec);
    given(headersSpec.header(anyString(), anyString())).willReturn(headersSpec);
    given(headersSpec.retrieve()).willReturn(responseSpec);
  }

  @Test
  @DisplayName("API에서 아이템 1개 반환 → When fetchNews → Then 매핑된 ExternalNewsItem")
  void apiResponse_whenFetchNews_thenMapped(){
    //given
    //응답 객체
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    NaverResponse response = new NaverResponse();
    NaverResponse.Item item = new NaverResponse.Item();
    item.originallink = "o1";
    item.link          = "l1";
    item.title         = "t1";
    item.pubDate      = formatter.format(ZonedDateTime.now());
    item.description   = "d1";
    response.items = List.of(item);

    given(responseSpec.bodyToMono(NaverResponse.class)).willReturn(Mono.just(response));

    //when
    List<ExternalNewsItem> result = client.fetchNews("kw");

    //then
    assertThat(result).hasSize(1)
        .extracting(ExternalNewsItem::title)
        .containsExactly("t1");
  }
  @Test
  @DisplayName("빈 Mono 반환 → When fetchNews → Then 빈 리스트 반환")
  void emptyResponse_whenFetchNews_thenEmptyList() {
    // given
    // 빈 Mono 스텁
    given(responseSpec.bodyToMono(NaverResponse.class))
        .willReturn(Mono.empty());

    // when
    // fetchNews 호출
    List<ExternalNewsItem> result = client.fetchNews("kw");

    // then
    // 빈 리스트인지 검증
    assertThat(result).isEmpty();
  }
}

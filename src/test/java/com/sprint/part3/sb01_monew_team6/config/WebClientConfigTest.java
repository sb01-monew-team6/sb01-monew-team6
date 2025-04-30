package com.sprint.part3.sb01_monew_team6.config;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientConfigTest {
  private final WebClientConfig config = new WebClientConfig();

  @Test
  @DisplayName("webClientBuilder 빈으로 WebClient.Builder 인스턴스 생성")
  void createWebClientBuilder() {
    //when
    WebClient.Builder builder = config.webClientBuilder();

    //then
    assertThat(builder).isNotNull();
    WebClient client = builder.build();
    assertThat(client).isNotNull();
    assertThat(client).isInstanceOf(WebClient.class);
  }
}

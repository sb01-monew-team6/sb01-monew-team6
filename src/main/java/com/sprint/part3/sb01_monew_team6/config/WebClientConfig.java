package com.sprint.part3.sb01_monew_team6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}

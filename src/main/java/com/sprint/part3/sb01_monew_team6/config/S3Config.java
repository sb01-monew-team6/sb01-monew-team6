package com.sprint.part3.sb01_monew_team6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

  @Bean
  public S3Client s3Client(){
    return S3Client.builder()
        .region()
        .credentialProvider(ProfileC)
  }
}

package com.sprint.part3.sb01_monew_team6.service.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.service.news.impl.ArticleServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
public class ArticleRestoreServiceTest {
  @Mock
  S3Client s3Client;
  @Mock
  NewsArticleRepository newsArticleRepository;
  @Mock
  ObjectMapper objectMapper;
  @InjectMocks
  ArticleServiceImpl service;

}

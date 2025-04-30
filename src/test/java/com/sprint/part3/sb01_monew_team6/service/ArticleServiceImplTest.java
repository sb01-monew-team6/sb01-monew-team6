//package com.sprint.part3.sb01_monew_team6.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//
//import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
//import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageResponseArticleDto;
//import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
//import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
//import com.sprint.part3.sb01_monew_team6.service.impl.ArticleServiceImpl;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//public class ArticleServiceImplTest {
//  @Mock
//  NewsArticleRepository newsArticleRepository;
//  @Mock
//  CommentRepository commentRepository;
//  @InjectMocks
//  ArticleServiceImpl articleService;
//
//  @Test
//  @DisplayName("레포지토리가 비어있을 때, 검색을 실행하면 null 발생")
//  void repositoryEmpty_thenNull(){
//    //given
//    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
//        .orderBy("publishDate")
//        .direction("DESC")
//        .limit(3)
//        .build();
//
//    given(newsArticleRepository.searchArticles(any(),any(),any(),any(),eq(3))).willReturn(List.of());
//
//    //when
//    CursorPageResponseArticleDto response = articleService.searchArticles(request);
//
//    //then
//    assertThat(response.content()).isNull();
//  }
//}

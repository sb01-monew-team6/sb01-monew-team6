package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.repository.ArticleViewRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleViewServiceTest {
  @Mock
  private NewsArticleRepository newsArticleRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private CommentRepository commentRepository;
  @Mock
  private ArticleViewRepository articleViewRepository;

  @InjectMocks
  private ArticleViewService service;


}

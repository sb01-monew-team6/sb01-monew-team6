package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    public CommentServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("댓글 등록 - 정상 등록 시 CommentDto 반환")
    @Test
    void registerComment_shouldReturnCommentDto() {
        //  given
        CommentRegisterRequest request = CommentRegisterRequest.builder()
                .articleId(1L)
                .userId(1L)
                .content("테스트 댓글입니다.")
                .build();

        NewsArticle newsArticle = new NewsArticle();
        newsArticle.setSource("테스트 출처");
        newsArticle.setSourceUrl("http://test.com");
        newsArticle.setArticleTitle("테스트 제목");
        newsArticle.setArticlePublishedDate(Instant.now());
        newsArticle.setArticleSummary("테스트 요약");

        User user = User.builder()
                .nickname("tester")
                .email("tester@example.com")
                .password("encodedPassword")
                .build();

        Comment comment = Comment.builder()
                        .user(user)
                                .article(newsArticle)
                                        .content(request.content())
                                                .build();


        given(newsArticleRepository.findById(1L)).willReturn(Optional.of(newsArticle));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // 아직 article, user, comment mocking 안함 (Green 단계에서 할 거야)

        //  when
        CommentDto result = commentService.register(request);

        //  then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("테스트 댓글입니다.");
    }
}

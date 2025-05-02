package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.CommentDto;
import com.sprint.part3.sb01_monew_team6.dto.CommentRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.repository.CommentLikeRepository;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.news.NewsArticleRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import com.sprint.part3.sb01_monew_team6.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

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

        NewsArticle article = createTestArticle();
        User user = createTestUser();
        Comment comment = createTestComment(user, article, request.content());


        given(newsArticleRepository.findById(1L)).willReturn(Optional.of(article));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        //  when
        CommentDto result = commentService.register(request);

        //  then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("테스트 댓글입니다.");
    }

    @DisplayName("댓글 목록 조회 - 정상 조회 시 댓글 리스트 반환")
    @Test
    void getComments_shouldReturnCommentList() {
        //  given
        NewsArticle article = createTestArticle();
        User user = createTestUser();
        List<Comment> commentList = List.of(
                createTestComment(user, article, "댓글 1"),
                createTestComment(user, article, "댓글 2")
        );

        given(newsArticleRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findAllByArticleId(1L)).willReturn(commentList);

        //  when
        List<CommentDto> result = commentService.getComments(1L);

        //  then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).content()).isEqualTo("댓글 1");
    }

    @DisplayName("댓글 목록 조회 - 페이지네이션 적용 시 댓글 리스트 반환")
    @Test
    void findAll_withPagination_shouldReturnPagedComments() {
        // given
        Long articleId = 1L;
        String orderBy = "createdAt";
        String direction = "DESC";
        Integer limit = 2;
        Long requestUserId = 1L;

        // 댓글 객체 생성
        Comment comment1 = createTestComment(createTestUser(), createTestArticle(), "댓글 1");
        Comment comment2 = createTestComment(createTestUser(), createTestArticle(), "댓글 2");
        Comment comment3 = createTestComment(createTestUser(), createTestArticle(), "댓글 3");
        List<Comment> commentList = new ArrayList<>(List.of(comment1, comment2, comment3));

        // mock 설정
        given(commentRepository.findAllByArticleId(articleId)).willReturn(commentList);
        given(commentLikeRepository.countByCommentId(comment1.getId())).willReturn(0L);
        given(commentLikeRepository.countByCommentId(comment2.getId())).willReturn(5L);
        given(commentLikeRepository.countByCommentId(comment3.getId())).willReturn(10L);
        given(commentLikeRepository.existsByCommentIdAndUserId(comment1.getId(), requestUserId)).willReturn(false);
        given(commentLikeRepository.existsByCommentIdAndUserId(comment2.getId(), requestUserId)).willReturn(true);
        given(commentLikeRepository.existsByCommentIdAndUserId(comment3.getId(), requestUserId)).willReturn(true);

        // when
        List<CommentDto> result = commentService.findAll(articleId, orderBy, direction, null, null, limit, requestUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);  // 페이지당 2개의 댓글이 반환되어야 함
        assertThat(result.get(0).content()).isEqualTo("댓글 1");  // 첫 번째 댓글
        assertThat(result.get(1).content()).isEqualTo("댓글 2");  // 두 번째 댓글
    }

    private NewsArticle createTestArticle() {
        NewsArticle article = new NewsArticle();
        article.setSource("테스트 출처");
        article.setSourceUrl("http://test.com");
        article.setArticleTitle("테스트 제목");
        article.setArticlePublishedDate(Instant.now());
        article.setArticleSummary("테스트 요약");
        return article;
    }

    private User createTestUser() {
        return User.builder()
                .nickname("tester")
                .email("tester@example.com")
                .password("encodedPassword")
                .build();
    }

    private Comment createTestComment(User user, NewsArticle article, String content) {
        return Comment.builder()
                .user(user)
                .article(article)
                .content(content)
                .build();
    }

}

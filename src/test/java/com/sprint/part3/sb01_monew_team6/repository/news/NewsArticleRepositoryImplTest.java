package com.sprint.part3.sb01_monew_team6.repository.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.sprint.part3.sb01_monew_team6.config.JpaConfig;
import com.sprint.part3.sb01_monew_team6.config.QueryDslConfig;
import com.sprint.part3.sb01_monew_team6.dto.news.CursorPageRequestArticleDto;
import com.sprint.part3.sb01_monew_team6.entity.Comment;
import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.NewsArticleInterest;
import com.sprint.part3.sb01_monew_team6.entity.QNewsArticle;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.repository.CommentRepository;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({ QueryDslConfig.class, JpaConfig.class })
@AutoConfigureTestDatabase(replace = NONE)
public class NewsArticleRepositoryImplTest {

  @Autowired
  private NewsArticleRepository newsArticleRepository;

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private NewsArticleInterestRepository newsArticleInterestRepository;
  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private UserRepository userRepository;

  private NewsArticle a1, a2, a3;
  private Interest iSport;
  private User testUser;
  private final QNewsArticle article = QNewsArticle.newsArticle;

  @BeforeEach
  void setUp() {
    testUser = userRepository.save(
        User.builder()
            .nickname("testuser")
            .email("test@example.com")
            .password("pass")
            .build()
    );
    // publishDate 순서: a1 < a2 < a3
    a1 = newsArticleRepository.save(NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("u1")
        .articleTitle("Java")
        .articlePublishedDate(Instant.parse("2025-04-01T00:00:00Z"))
        .articleSummary("s1")
        .build()
    );
    a2 = newsArticleRepository.save(NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("u2")
        .articleTitle("Spring")
        .articlePublishedDate(Instant.parse("2025-04-02T00:00:00Z"))
        .articleSummary("s2")
        .build()
    );
    a3 = newsArticleRepository.save(NewsArticle.builder()
        .source("OTHER")
        .sourceUrl("u3")
        .articleTitle("Hibernate")
        .articlePublishedDate(Instant.parse("2025-04-03T00:00:00Z"))
        .articleSummary("s3")
        .build()
    );

    iSport = interestRepository.save(
        Interest.builder()
            .name("sports")
            .keywords("ball")
            .build()
    );

    newsArticleInterestRepository.save(new NewsArticleInterest(a2, iSport));
  }

  @Test
  @DisplayName("검색어 없이 주어진 조건(DESC, limit=2)이면 Hibernate, Spring 반환")
  void search_noKeyword_latestDesc_limit2() {
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .limit(2)
        .build();

    OrderSpecifier<Instant> publishDateDesc = new OrderSpecifier<>(Order.DESC, article.articlePublishedDate);

    List<NewsArticle> result =
        newsArticleRepository.searchArticles(request, publishDateDesc, null, null, 2);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("Hibernate");
    assertThat(result.get(1).getArticleTitle()).isEqualTo("Spring");;
  }

  @Test
  @DisplayName("검색어 없이 주어진 조건(ASC, limit=2)이면 Java, Spring 반환")
  void search_noKeyword_asc_limit2() {
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("ASC")
        .limit(2)
        .build();

    OrderSpecifier<Instant> publishDateAsc = new OrderSpecifier<>(Order.ASC, article.articlePublishedDate);

    List<NewsArticle> result =
        newsArticleRepository.searchArticles(request, publishDateAsc, null, null, 2);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("Java");
    assertThat(result.get(1).getArticleTitle()).isEqualTo("Spring");
  }

  @Test
  @DisplayName("키워드 'H'로 검색하면 Hibernate만 반환")
  void search_withKeyword_onlyHibernate() {
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .keyword("H")
        .limit(10)
        .build();

    OrderSpecifier<Instant> publishDateDesc = new OrderSpecifier<>(Order.DESC, article.articlePublishedDate);

    List<NewsArticle> result =
        newsArticleRepository.searchArticles(request, publishDateDesc, null, null, 10);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getArticleTitle()).isEqualTo("Hibernate");
  }

  @Test
  @DisplayName("전체 범위 publishDateFrom/To 지정 시 countArticles 반환값 확인")
  void count_withDateRange() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .publishDateFrom(Instant.parse("2025-04-01T00:00:00Z"))
        .publishDateTo(Instant.parse("2025-04-03T23:59:59Z"))
        .build();

    long total = newsArticleRepository.countArticles(req);
    assertThat(total).isEqualTo(3L);
  }

  @Test
  @DisplayName("interestId 필터링 결과 확인 (Spring만 반환)")
  void search_withInterestId_onlySpring() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .interestId(iSport.getId())
        .orderBy("publishDate")
        .direction("DESC")
        .limit(10)
        .build();

    OrderSpecifier<Instant> publishDateDesc = new OrderSpecifier<>(Order.DESC, article.articlePublishedDate);

    List<NewsArticle> result =
        newsArticleRepository.searchArticles(req, publishDateDesc, null, null, 10);

    assertThat(result).hasSize(1)
        .extracting(NewsArticle::getArticleTitle)
        .containsExactly("Spring");
  }

  @Test
  @DisplayName("sourceIn 필터링 결과 확인 (NAVER만 반환)")
  void search_withSourceIn_onlyNaver() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .sourceIn(List.of("NAVER"))
        .orderBy("publishDate")
        .direction("DESC")
        .limit(10)
        .build();

    OrderSpecifier<Instant> publishDateDesc = new OrderSpecifier<>(Order.DESC, article.articlePublishedDate);

    List<NewsArticle> list =
        newsArticleRepository.searchArticles(req, publishDateDesc, null, null, 10);

    assertThat(list).extracting(NewsArticle::getSource)
        .containsExactly("NAVER", "NAVER");
  }

  @Test
  @DisplayName("publishDateFrom 기준 필터링 (NewPost만 반환)")
  void search_withPublishDateFrom_onlyNewPost() {
    NewsArticle a4 = newsArticleRepository.save(NewsArticle.builder()
        .source("NAVER")
        .sourceUrl("u4")
        .articleTitle("NewPost")
        .articlePublishedDate(Instant.parse("2025-04-04T00:00:00Z"))
        .articleSummary("s4")
        .build());

    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .publishDateFrom(Instant.parse("2025-04-04T00:00:00Z"))
        .orderBy("publishDate")
        .direction("ASC")
        .limit(10)
        .build();

    OrderSpecifier<Instant> publishDateAsc = new OrderSpecifier<>(Order.ASC, article.articlePublishedDate);

    List<NewsArticle> list =
        newsArticleRepository.searchArticles(req, publishDateAsc, null, null, 10);

    assertThat(list).containsExactly(a4);
  }

  @Test
  @DisplayName("cursor 기반 페이징 (Hibernate -> Spring 순서)")
  void search_withCursorPaging() {
    CursorPageRequestArticleDto page1Req = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .limit(1)
        .build();

    OrderSpecifier<Instant> publishDateDesc = new OrderSpecifier<>(Order.DESC, article.articlePublishedDate);
    List<NewsArticle> page1 = newsArticleRepository.searchArticles(page1Req, publishDateDesc, null, null, 1);

    assertThat(page1).hasSize(1)
        .extracting(NewsArticle::getArticleTitle)
        .containsExactly("Hibernate");

    CursorPageRequestArticleDto page2Req = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .cursor(page1.get(0).getId().toString())  // ← 여기가 핵심 변경점
        .limit(1)
        .build();

    List<NewsArticle> page2 = newsArticleRepository.searchArticles(page2Req, publishDateDesc, null, null, 1);
    assertThat(page2).hasSize(1)
        .extracting(NewsArticle::getArticleTitle)
        .containsExactly("Spring");
  }

  @Test
  @DisplayName("isDeleted=true인 항목 제외 확인")
  void search_excludesDeleted() {
    a2.setDeleted(true);
    newsArticleRepository.save(a2);

    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("id")
        .direction("ASC")
        .limit(10)
        .build();

    OrderSpecifier<Long> idAsc = new OrderSpecifier<>(Order.ASC, article.id);

    List<NewsArticle> list =
        newsArticleRepository.searchArticles(req, idAsc, null, null, 10);

    assertThat(list).extracting(NewsArticle::getId)
        .containsExactly(a1.getId(), a3.getId());
  }

  @Test
  @DisplayName("외부 OrderSpecifier 우선 적용 확인 (title ASC)")
  void search_withExternalOrderSpec() {
    OrderSpecifier<String> titleAsc =
        new OrderSpecifier<>(Order.ASC, article.articleTitle);

    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("DESC")
        .limit(10)
        .build();

    List<NewsArticle> list =
        newsArticleRepository.searchArticles(req, titleAsc, null, null, 10);

    assertThat(list).extracting(NewsArticle::getArticleTitle)
        .containsExactly("Hibernate", "Java", "Spring");
  }

  @Test
  @DisplayName("countArticles: keyword 필터링")
  void count_withKeyword() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .keyword("Java")
        .build();
    assertThat(newsArticleRepository.countArticles(req)).isEqualTo(1L);
  }

  @Test
  @DisplayName("countArticles: interestId 필터링")
  void count_withInterest() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .interestId(iSport.getId())
        .build();
    assertThat(newsArticleRepository.countArticles(req)).isEqualTo(1L);
  }

  @Test
  @DisplayName("searchArticles: publishDateTo 기준 필터링 (과거 게시물만 반환)")
  void search_withPublishDateTo_onlyOldPosts() {
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .publishDateTo(Instant.parse("2025-04-02T23:59:59Z"))
        .orderBy("publishDate")
        .direction("ASC")
        .limit(10)
        .build();

    OrderSpecifier<Instant> publishDateAsc = new OrderSpecifier<>(Order.ASC, article.articlePublishedDate);

    List<NewsArticle> list = newsArticleRepository.searchArticles(
        request,
        publishDateAsc,
        null,
        null,
        10
    );

    assertThat(list).extracting(NewsArticle::getSourceUrl)
        .containsExactly("u1", "u2");
  }
  @Test
  @DisplayName("searchArticles: after만 있을 때 (최근 이후 게시물)")
  void search_withAfterOnly() {
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate")
        .direction("ASC")
        .after(Instant.parse("2025-04-01T12:00:00Z"))
        .limit(10)
        .build();

    OrderSpecifier<Instant> publishDateAsc = new OrderSpecifier<>(Order.ASC, article.articlePublishedDate);

    List<NewsArticle> list = newsArticleRepository.searchArticles(
        request,
        publishDateAsc,
        null,
        request.after(),
        10
    );

    //when,then
    assertThat(list).extracting(NewsArticle::getSourceUrl)
        .containsExactly("u2", "u3");
  }

  @Test
  @DisplayName("countArticles: publishDateFrom만 있을 때 개수 반환")
  void count_withDateFromOnly() {
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .publishDateFrom(Instant.parse("2025-04-02T00:00:00Z"))
        .build();

    long count = newsArticleRepository.countArticles(request);

    assertThat(count).isEqualTo(2L);
  }

  @Test
  @DisplayName("countArticles: publishDateTo만 있을 때 개수 반환")
  void count_withDateToOnly() {
    //given
    CursorPageRequestArticleDto request = CursorPageRequestArticleDto.builder()
        .publishDateTo(Instant.parse("2025-04-02T23:59:59Z"))
        .build();

    long count = newsArticleRepository.countArticles(request);

    //when,then
    assertThat(count).isEqualTo(2L);
  }
  @Test
  @DisplayName("commentCount 기준 DESC 정렬")
  void search_withCommentCountDesc() {
    // a4에 댓글 2개
    NewsArticle a4 = newsArticleRepository.save(NewsArticle.builder()
        .source("NAVER").sourceUrl("u4")
        .articleTitle("WithComments")
        .articlePublishedDate(Instant.parse("2025-04-05T00:00:00Z"))
        .articleSummary("s4").build());

    commentRepository.save(Comment.builder()
        .article(a4).user(testUser).content("c1").isDeleted(false).build());
    commentRepository.save(Comment.builder()
        .article(a4).user(testUser).content("c2").isDeleted(false).build());
    // a3에 댓글 1개
    commentRepository.save(Comment.builder()
        .article(a3).user(testUser).content("c3").isDeleted(false).build());

    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("commentCount").direction("DESC").limit(3).build();
    OrderSpecifier<Long> dummy = new OrderSpecifier<>(Order.DESC, article.id);

    List<NewsArticle> list =
        newsArticleRepository.searchArticles(req, dummy, null, null, 3);

    assertThat(list).extracting(NewsArticle::getArticleTitle)
        .containsExactly("WithComments", "Hibernate", "Spring");
  }

  @Test
  @DisplayName("commentCount 기준 ASC 정렬")
  void search_withCommentCountAsc() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .orderBy("commentCount").direction("ASC").limit(3).build();
    OrderSpecifier<Long> dummy = new OrderSpecifier<>(Order.ASC, article.id);

    List<NewsArticle> list =
        newsArticleRepository.searchArticles(req, dummy, null, null, 3);

    assertThat(list).extracting(NewsArticle::getArticleTitle)
        .containsExactly("Hibernate", "Spring", "Java");
  }

  @Test
  @DisplayName("after+cursor 동시 사용 시 커서 페이징")
  void search_withAfterAndCursor() {
    // page1: 가장 오래된 1건(ASC)
    CursorPageRequestArticleDto p1 = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate").direction("ASC").limit(1).build();
    OrderSpecifier<Instant> pubAsc =
        new OrderSpecifier<>(Order.ASC, QNewsArticle.newsArticle.articlePublishedDate);
    List<NewsArticle> page1 = newsArticleRepository.searchArticles(p1, pubAsc, null, null, 1);

    Instant after = page1.get(0).getArticlePublishedDate();
    // String으로만 쓸 게 아니라, ID(Long)도 같이 저장
    Long cursorId = page1.get(0).getId();
    String cursorStr = cursorId.toString();

    // page2: after+cursor 조합
    CursorPageRequestArticleDto p2 = CursorPageRequestArticleDto.builder()
        .orderBy("publishDate").direction("ASC")
        .after(after)
        .cursor(cursorStr)  // DTO에는 String으로 세팅
        .limit(1)
        .build();

    // repository 호출 시엔 Long 타입 cursorId를 넘겨야 함
    List<NewsArticle> page2 = newsArticleRepository.searchArticles(
        p2, pubAsc, cursorId, after, 1);

    assertThat(page2).hasSize(1)
        .extracting(NewsArticle::getArticleTitle)
        .containsExactly("Spring");
  }

  @Test
  @DisplayName("countArticles: sourceIn 필터링")
  void count_withSourceIn() {
    CursorPageRequestArticleDto req = CursorPageRequestArticleDto.builder()
        .sourceIn(List.of("NAVER")).build();
    long cnt = newsArticleRepository.countArticles(req);
    // setUp()에서 NAVER 기사 2건이므로
    assertThat(cnt).isEqualTo(2L);
  }
}

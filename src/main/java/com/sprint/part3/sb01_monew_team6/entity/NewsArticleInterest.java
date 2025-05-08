package com.sprint.part3.sb01_monew_team6.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "news_article_interest",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"news_article_id", "interest_id"})
    }
)
@NoArgsConstructor
@Getter @Setter
public class NewsArticleInterest {

  @EmbeddedId
  private NewsArticleInterestId id; //복합 기본 키

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("newsArticleId") //복합 기본 키의 일부를 외래 키로 매핑
  @JoinColumn(name = "news_article_id", nullable = false)
  private NewsArticle newsArticle; // 기사와의 다대일 관계 매핑

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("interestId") //복합 기본 키의 일부를 외래 키로 매핑
  @JoinColumn(name = "interest_id", nullable = false)
  private Interest interest; // 관심사와의 다대일 관계 매핑

  @Column(name = "created_at", nullable = false,
      columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
  private Instant createdAt = Instant.now();

  public NewsArticleInterest(NewsArticle newsArticle, Interest interest) {
    this.newsArticle = newsArticle;
    this.interest    = interest;
    this.id          = new NewsArticleInterestId(
        newsArticle.getId(),
        interest.getId());
  }
}
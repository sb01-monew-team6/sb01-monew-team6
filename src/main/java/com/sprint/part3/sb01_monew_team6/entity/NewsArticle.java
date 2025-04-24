package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "news_article")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewsArticle extends BaseEntity {

  @Column(nullable = false)
  private String source;

  @Column(nullable = false)
  private String sourceUrl;

  @Column(nullable = false)
  private String articleTitle;

  @Column(nullable = false)
  private Instant articlePublishedDate;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String articleSummary;

  @Column(nullable = false)
  private boolean isDeleted = false;

  @ManyToMany
  @JoinTable(
      name = "news_article_interest",
      joinColumns = @JoinColumn(name = "news_article_id"),
      inverseJoinColumns = @JoinColumn(name = "interest_id")
  )
  private Set<Interest> interests = new HashSet<>();

  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ArticleView> articleViews = new ArrayList<>();

  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();
}
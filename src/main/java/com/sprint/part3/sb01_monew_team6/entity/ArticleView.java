package com.sprint.part3.sb01_monew_team6.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_view")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArticleView{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private NewsArticle article;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id",    nullable = false)
  private User user;

  @Column(name = "article_view_date", nullable = false)
  private Instant articleViewDate;
}

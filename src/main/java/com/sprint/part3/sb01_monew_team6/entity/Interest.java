package com.sprint.part3.sb01_monew_team6.entity;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Interest extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true)
  private String name;

  @ElementCollection
  @CollectionTable(name = "interest_keywords", joinColumns = @JoinColumn(name = "interest_id"))
  private List<String> keyword;

  @Column(nullable = false)
  private Long subscriberCount = 0L;

  @ManyToMany(mappedBy = "interests")
  private Set<NewsArticle> articles = new HashSet<>();
}


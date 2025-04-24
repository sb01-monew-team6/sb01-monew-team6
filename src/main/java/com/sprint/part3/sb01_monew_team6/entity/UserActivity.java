package com.sprint.part3.sb01_monew_team6.entity;

import static lombok.AccessLevel.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.sprint.part3.sb01_monew_team6.entity.base.BaseDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class UserActivity extends BaseDocument {

	private Long userId;
	private String email;
	private String nickName;

	private List<SubscriptionHistory> subscriptions;
	private List<CommentHistory> comments;
	private List<CommentLikesHistory> commentLikes;
	private List<ArticleViewHistory> articleViews;

	@Document
	@Getter
	@Builder
	@NoArgsConstructor(access = PROTECTED)
	@AllArgsConstructor
	public static class SubscriptionHistory {
		private Long id;
		private Long interestId;
		private String interestName;
		private List<String> interestKeywords;
		private Long interestSubscriberCount;
		private Instant createdAt;
	}

	@Document
	@Getter
	@Builder
	@NoArgsConstructor(access = PROTECTED)
	@AllArgsConstructor
	public static class CommentHistory {
		private Long id;
		private Long articleId;
		private String articleTitle;
		private Long userId;
		private String userNickname;
		private String content;
		private Long likeCount;
		private Instant createdAt;
	}

	@Document
	@Getter
	@Builder
	@NoArgsConstructor(access = PROTECTED)
	@AllArgsConstructor
	public static class CommentLikesHistory {
		private Long id;
		private Instant createdAt;
		private Long commentId;
		private Long articleId;
		private String articleTitle;
		private Long commentUserId;
		private String commentUserNickname;
		private String commentContent;
		private Long commentLikeCount;
		private Instant commentCreatedAt;
	}

	@Document
	@Getter
	@Builder
	@NoArgsConstructor(access = PROTECTED)
	@AllArgsConstructor
	public static class ArticleViewHistory {
		private Long id;
		private Long viewedBy;
		private Instant createdAt;
		private Long articleId;
		private String source;
		private String sourceUrl;
		private String articleTitle;
		private LocalDateTime articlePublishedDate;
		private String articleSummary;
		private Long articleCommentCount;
		private Long articleViewCount;
	}
}

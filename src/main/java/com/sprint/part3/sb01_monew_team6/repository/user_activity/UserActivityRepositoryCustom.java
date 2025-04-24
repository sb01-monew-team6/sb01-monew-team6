package com.sprint.part3.sb01_monew_team6.repository.user_activity;

import com.sprint.part3.sb01_monew_team6.entity.UserActivity;

public interface UserActivityRepositoryCustom {

	void addSubscription(Long userId, UserActivity.SubscriptionHistory subscription);

	void removeSubscription(Long userId, Long interestId);

	void addCommentLike(Long userId, UserActivity.CommentLikeHistory commentLike);

	void removeCommentLike(Long userId, Long commentId);

	void addComment(Long userId, UserActivity.CommentHistory comment);

	void removeComment(Long userId, Long articleId);

	void addArticleView(Long userId, UserActivity.ArticleViewHistory articleView);

	void removeArticleView(Long userId, Long viewedBy);

}

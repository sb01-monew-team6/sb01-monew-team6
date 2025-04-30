package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.dto.user_activity.CommentHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

@Component
public class ArticleViewHistoryValidator implements UserActivityValidator<ArticleViewHistoryDto> {

	@Override
	public void validate(ArticleViewHistoryDto articleView) {
		if (Objects.isNull(articleView)) {
			throw new UserActivityDomainException("기사 조회 가 null 일 수 없습니다.",
				Map.of("articleView", String.valueOf(articleView)));
		}

		if (Objects.isNull(articleView.articleId()) || articleView.articleId() <= 0) {
			throw new UserActivityDomainException("기사 id 가 유효하지 않습니다.",
				Map.of("articleId", String.valueOf(articleView.articleId())));
		}

		if (Objects.isNull(articleView.viewedBy()) || articleView.viewedBy() <= 0) {
			throw new UserActivityDomainException("기사 조회 유저 id 가 유효하지 않습니다.",
				Map.of("viewedBy", String.valueOf(articleView.viewedBy())));
		}
	}
}

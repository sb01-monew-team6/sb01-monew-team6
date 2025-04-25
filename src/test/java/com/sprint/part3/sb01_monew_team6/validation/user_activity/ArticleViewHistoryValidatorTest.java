package com.sprint.part3.sb01_monew_team6.validation.user_activity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.part3.sb01_monew_team6.dto.user_activity.ArticleViewHistoryDto;
import com.sprint.part3.sb01_monew_team6.exception.user_activity.UserActivityDomainException;

class ArticleViewHistoryValidatorTest {

	private final ArticleViewHistoryValidator validator = new ArticleViewHistoryValidator();

	@Test
	@DisplayName("validateViewHistoryDto 호출 시 ArticleView 가 null 이면 UserActivityDomainException 발생")
	public void throwUserActivityDomainExceptionWhenArticleViewIsNullWhileValidateViewHistoryDto() throws Exception {
		//given
		ArticleViewHistoryDto dto = null;

		//when & then
		assertThatThrownBy(() ->
			validator.validate(dto)
		).isInstanceOf(UserActivityDomainException.class);
	}
}
package com.sprint.part3.sb01_monew_team6.dto.user_activity;

import java.util.List;

public record SubscriptionHistoryDto(
	Long interestId,
	String interestName,
	List<String> interestKeywords,
	Long interestSubscriberCount
) {
}

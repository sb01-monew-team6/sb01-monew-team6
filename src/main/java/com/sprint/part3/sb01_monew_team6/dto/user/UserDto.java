package com.sprint.part3.sb01_monew_team6.dto.user;

import java.time.Instant;

public record UserDto(
	Long id,
	String email,
	String nickname,
	Instant createdAt
) {
}

package com.sprint.part3.sb01_monew_team6.dto;

import com.sprint.part3.sb01_monew_team6.entity.User; // User 엔티티 임포트
import java.time.Instant;

public record UserDto(
    Long id,
    String email,
    String nickname,
    Instant createdAt
) {
  public static UserDto fromEntity(User user) {
    if (user == null) {
      return null;
    }
    return new UserDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt()
    );
  }
}
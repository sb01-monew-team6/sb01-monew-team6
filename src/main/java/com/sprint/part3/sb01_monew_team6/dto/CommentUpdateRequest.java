package com.sprint.part3.sb01_monew_team6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @Size(min = 1, max = 500) @NotBlank String content
) {
}

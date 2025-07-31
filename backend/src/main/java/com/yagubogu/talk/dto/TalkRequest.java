package com.yagubogu.talk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TalkRequest(
        @NotNull long memberId,
        @NotBlank String content
) {
}

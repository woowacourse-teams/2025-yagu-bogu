package com.yagubogu.talk.dto;

import jakarta.validation.constraints.NotBlank;

public record TalkRequest(
        @NotBlank String content
) {
}

package com.yagubogu.talk.dto.v1;

import jakarta.validation.constraints.NotBlank;

public record TalkRequest(
        @NotBlank String content
) {
}

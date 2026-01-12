package com.yagubogu.talk.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TalkRequest(
        @NotBlank(message = "clientMessageId는 필수입니다")
        String clientMessageId,

        @NotBlank(message = "내용은 필수입니다")
        @Size(max = 500, message = "내용은 500자를 초과할 수 없습니다")
        String content
) {
}

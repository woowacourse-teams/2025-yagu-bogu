package com.yagubogu.talk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TalkRequest(
        @NotNull long memberId, // TODO: 아이디 삭제
        @NotBlank String content
) {
}

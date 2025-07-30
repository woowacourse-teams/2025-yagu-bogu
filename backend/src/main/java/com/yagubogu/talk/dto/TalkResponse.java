package com.yagubogu.talk.dto;

import java.time.LocalDateTime;

public record TalkResponse(
        long id,
        long memberId,
        String nickname,
        String favorite,
        String content,
        LocalDateTime createdAt
) {
}

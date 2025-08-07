package com.yagubogu.talk.dto;

import com.yagubogu.talk.domain.Talk;
import java.time.LocalDateTime;

public record TalkResponse(
        long id,
        long memberId,
        String nickname,
        String favorite,
        String content,
        LocalDateTime createdAt,
        boolean isMine
) {

    public static TalkResponse from(Talk talk, long memberId) {
        return new TalkResponse(
                talk.getId(),
                talk.getMember().getId(),
                talk.getMember().getNickname(),
                talk.getMember().getTeam().getShortName(),
                talk.getContent(),
                talk.getCreatedAt(),
                talk.getMember().getId().equals(memberId)
        );
    }

    public static TalkResponse hiddenFrom(TalkResponse talkResponse) {
        return new TalkResponse(
                talkResponse.id(),
                talkResponse.memberId(),
                talkResponse.nickname(),
                talkResponse.favorite(),
                "숨김처리되었습니다",
                talkResponse.createdAt(),
                talkResponse.isMine()
        );
    }
}

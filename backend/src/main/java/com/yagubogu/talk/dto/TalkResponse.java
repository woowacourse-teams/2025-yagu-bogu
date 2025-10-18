package com.yagubogu.talk.dto;

import com.yagubogu.talk.domain.Talk;
import java.time.LocalDateTime;

public record TalkResponse(
        long id,
        Long memberId,
        String nickname,
        String favorite,
        String imageUrl,
        String content,
        LocalDateTime createdAt,
        boolean isMine
) {

    public static TalkResponse from(Talk talk, long memberId) {
        if (talk.getMember() == null) {
            return new TalkResponse(
                    talk.getId(),
                    null,
                    null,
                    null,
                    null,
                    talk.getContent(),
                    talk.getCreatedAt(),
                    false
            );
        }

        return new TalkResponse(
                talk.getId(),
                talk.getMember().getId(),
                talk.getMember().getNickname().getValue(),
                talk.getMember().getTeam().getShortName(),
                talk.getMember().getImageUrl(),
                talk.getContent(),
                talk.getCreatedAt(),
                talk.getMember().isSameId(memberId)
        );
    }

    public static TalkResponse hiddenFrom(TalkResponse talkResponse) {
        return new TalkResponse(
                talkResponse.id(),
                talkResponse.memberId(),
                talkResponse.nickname(),
                talkResponse.favorite(),
                talkResponse.imageUrl(),
                "숨김처리되었습니다",
                talkResponse.createdAt(),
                talkResponse.isMine()
        );
    }
}

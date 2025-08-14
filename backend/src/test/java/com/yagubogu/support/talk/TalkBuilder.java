package com.yagubogu.support.talk;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.talk.domain.Talk;
import java.time.LocalDateTime;

public class TalkBuilder {

    private Game game;
    private Member member;
    private String content = "talk content";
    private LocalDateTime createdAt = LocalDateTime.now();

    public TalkBuilder game(final Game game) {
        this.game = game;

        return this;
    }

    public TalkBuilder member(final Member member) {
        this.member = member;

        return this;
    }

    public TalkBuilder content(final String content) {
        this.content = content;

        return this;
    }

    public TalkBuilder createdAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;

        return this;
    }

    public Talk build() {
        return new Talk(game, member, content, createdAt);
    }
}

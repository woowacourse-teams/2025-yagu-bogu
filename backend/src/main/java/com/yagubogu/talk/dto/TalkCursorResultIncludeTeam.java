package com.yagubogu.talk.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;

public record TalkCursorResultIncludeTeam(
        String stadiumName,
        String homeTeamCode,
        String awayTeamCode,
        String myTeamCode,
        CursorResult<TalkResponse> cursorResult
) {

    public static TalkCursorResultIncludeTeam from(
            Game game,
            Member member,
            CursorResult<TalkResponse> cursorResult
    ) {
        return new TalkCursorResultIncludeTeam(
                game.getStadium().getFullName(),
                game.getHomeTeam().getTeamCode(),
                game.getAwayTeam().getTeamCode(),
                member.getTeam().getTeamCode(),
                cursorResult
        );
    }
}

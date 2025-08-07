package com.yagubogu.talk.dto;

import com.yagubogu.game.domain.Game;

public record TalkCursorResult(
        String stadiumName,
        String homeTeamName,
        String awayTeamName,
        CursorResult<TalkResponse> cursorResult
) {

    public static TalkCursorResult from(
            Game game,
            CursorResult<TalkResponse> cursorResult
    ) {
        return new TalkCursorResult(
                game.getStadium().getFullName(),
                game.getHomeTeam().getShortName(),
                game.getAwayTeam().getShortName(),
                cursorResult
        );
    }
}

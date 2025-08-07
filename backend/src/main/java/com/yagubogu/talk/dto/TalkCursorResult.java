package com.yagubogu.talk.dto;

public record TalkCursorResult(
        String stadiumName,
        String homeTeamName,
        String awayTeamName,
        CursorResult<TalkResponse> cursorResult
) {

    public static TalkCursorResult from(
            String stadiumName,
            String homeTeamName,
            String awayTeamName,
            CursorResult<TalkResponse> cursorResult
    ) {
        return new TalkCursorResult(
                stadiumName,
                homeTeamName,
                awayTeamName,
                cursorResult
        );
    }
}

package com.yagubogu.talk.dto.v1;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;

public record TalkEntranceResponse(
        String stadiumName,
        String homeTeamCode,
        String awayTeamCode,
        String myTeamCode
) {

    public static TalkEntranceResponse from(
            Game game,
            Member member
    ) {
        return new TalkEntranceResponse(
                game.getStadium().getFullName(),
                game.getHomeTeam().getTeamCode(),
                game.getAwayTeam().getTeamCode(),
                member.getTeam().getTeamCode()
        );
    }
}

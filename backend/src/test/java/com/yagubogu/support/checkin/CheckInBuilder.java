package com.yagubogu.support.checkin;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.team.domain.Team;

public class CheckInBuilder {

    private Game game;
    private Member member;
    private Team team;
    private CheckInType checkInType = CheckInType.LOCATION_CHECK_IN;

    public CheckInBuilder game(final Game game) {
        this.game = game;

        return this;
    }

    public CheckInBuilder member(final Member member) {
        this.member = member;

        return this;
    }

    public CheckInBuilder team(final Team team) {
        this.team = team;

        return this;
    }

    public CheckInBuilder checkInType(final CheckInType checkInType) {
        this.checkInType = checkInType;

        return this;
    }

    public CheckIn build() {
        return new CheckIn(game, member, team, checkInType);
    }
}

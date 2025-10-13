package com.yagubogu.support.pastcheckin;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.pastcheckin.domain.PastCheckIn;
import com.yagubogu.team.domain.Team;

public class PastCheckInBuilder {

    private Game game;
    private Member member;
    private Team team;

    public PastCheckInBuilder game(final Game game) {
        this.game = game;
        return this;
    }

    public PastCheckInBuilder member(final Member member) {
        this.member = member;
        return this;
    }

    public PastCheckInBuilder team(final Team team) {
        this.team = team;
        return this;
    }

    public PastCheckIn build() {
        return new PastCheckIn(game, member, team);
    }
}

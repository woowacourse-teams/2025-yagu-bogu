package com.yagubogu.support;

import com.yagubogu.game.domain.ScoreBoardSummary;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import com.yagubogu.team.domain.Team;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestFixture {

    public static LocalDate getToday() {
        return LocalDate.of(2025, 7, 21);
    }

    public static LocalDate getYesterday() {
        return getToday().minusDays(1);
    }

    public static LocalDate getInvalidDate() {
        return LocalDate.of(1000, 6, 15);
    }

    public static LocalTime getStartTime() {
        return LocalTime.of(18, 30);
    }

    public static ScoreBoardSummary getHomeScoreBoard() {
        return new ScoreBoardSummary(10, 10, 10, 10);
    }

    public static ScoreBoardSummary getAwayScoreBoard() {
        return new ScoreBoardSummary(1, 1, 1, 1);
    }

    public static Member getUser(Team team) {
        return new Member(team, "김도영", "email", OAuthProvider.GOOGLE, "sub", Role.USER,
                "picture");
    }

    public static Team getTeam() {
        return new Team("한화 이글스", "한화", "HH");
    }

    public static Instant getAfter60Minutes() {
        return Instant.now().plusSeconds(3600);
    }
}

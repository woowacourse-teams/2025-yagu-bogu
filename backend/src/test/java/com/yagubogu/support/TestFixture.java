package com.yagubogu.support;

import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.team.domain.Team;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    public static ScoreBoard getHomeScoreBoard() {
        return new ScoreBoard(10, 10, 10, 10,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-"));
    }

    public static ScoreBoard getHomeScoreBoardAbout(Integer runs) {
        return new ScoreBoard(runs, 10, 10, 10,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-"));
    }

    public static ScoreBoard getAwayScoreBoard() {
        return new ScoreBoard(1, 1, 1, 1,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-"));
    }

    public static ScoreBoard getAwayScoreBoardAbout(Integer runs) {
        return new ScoreBoard(runs, 10, 10, 10,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-"));
    }

    public static Team getTeam() {
        return new Team("한화 이글스", "한화", "HH");
    }

    public static Instant getAfter60Minutes() {
        return Instant.now().plusSeconds(3600);
    }
}

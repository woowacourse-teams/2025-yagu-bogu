package com.yagubogu.support.game;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoardSummary;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class GameBuilder {

    private Stadium stadium;
    private Team homeTeam;
    private Team awayTeam;
    private LocalDate date = LocalDate.now();
    private LocalTime startAt = LocalTime.of(18, 30);
    private String gameCode = UUID.randomUUID().toString();
    private Integer homeScore;
    private Integer awayScore;
    private ScoreBoardSummary homeScoreBoardSummary;
    private ScoreBoardSummary awayScoreBoardSummary;
    private GameState gameState = GameState.SCHEDULED;

    public GameBuilder stadium(final Stadium stadium) {
        this.stadium = stadium;

        return this;
    }

    public GameBuilder homeTeam(final Team homeTeam) {
        this.homeTeam = homeTeam;

        return this;
    }

    public GameBuilder awayTeam(final Team awayTeam) {
        this.awayTeam = awayTeam;

        return this;
    }

    public GameBuilder date(final LocalDate date) {
        this.date = date;

        return this;
    }

    public GameBuilder startAt(final LocalTime startAt) {
        this.startAt = startAt;

        return this;
    }

    public GameBuilder gameCode(final String gameCode) {
        this.gameCode = gameCode;

        return this;
    }

    public GameBuilder homeScore(final Integer homeScore) {
        this.homeScore = homeScore;

        return this;
    }

    public GameBuilder awayScore(final Integer awayScore) {
        this.awayScore = awayScore;

        return this;
    }

    public GameBuilder homeScoreBoardSummary(final ScoreBoardSummary homeScoreBoardSummary) {
        this.homeScoreBoardSummary = homeScoreBoardSummary;

        return this;
    }

    public GameBuilder awayScoreBoardSummary(final ScoreBoardSummary awayScoreBoardSummary) {
        this.awayScoreBoardSummary = awayScoreBoardSummary;

        return this;
    }

    public GameBuilder gameState(final GameState gameState) {
        this.gameState = gameState;

        return this;
    }

    public Game build() {
        return new Game(
                stadium,
                homeTeam,
                awayTeam,
                date,
                startAt,
                gameCode,
                homeScore,
                awayScore,
                homeScoreBoardSummary,
                awayScoreBoardSummary,
                gameState
        );
    }
}

package com.yagubogu.support.game;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
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
    private GameState gameState = GameState.SCHEDULED;
    private ScoreBoard homeScoreBoard;
    private ScoreBoard awayScoreBoard;
    private String homePitcher;
    private String awayPitcher;

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

    public GameBuilder homePitcher(final String homePitcher) {
        this.homePitcher = homePitcher;

        return this;
    }

    public GameBuilder awayPitcher(final String awayPitcher) {
        this.awayPitcher = awayPitcher;

        return this;
    }

    public GameBuilder homeScoreBoard(final ScoreBoard homeScoreBoard) {
        this.homeScoreBoard = homeScoreBoard;

        return this;
    }

    public GameBuilder awayScoreBoard(final ScoreBoard awayScoreBoard) {
        this.awayScoreBoard = awayScoreBoard;

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
                homeScoreBoard,
                awayScoreBoard,
                homePitcher,
                awayPitcher,
                gameState
        );
    }
}

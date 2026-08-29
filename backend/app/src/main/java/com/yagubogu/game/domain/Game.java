package com.yagubogu.game.domain;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "games")
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;

    @Column(name = "game_code", nullable = false, unique = true)
    private String gameCode;

    @Column(name = "home_score", nullable = true)
    private Integer homeScore;

    @Column(name = "away_score", nullable = true)
    private Integer awayScore;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "home_score_board_id")
    private ScoreBoard homeScoreBoard;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "away_score_board_id")
    private ScoreBoard awayScoreBoard;

    @Column(name = "home_pitcher", nullable = true)
    private String homePitcher;

    @Column(name = "away_pitcher", nullable = true)
    private String awayPitcher;

    @Column(name = "home_probable_pitcher", nullable = true)
    private String homeProbablePitcher;

    @Column(name = "away_probable_pitcher", nullable = true)
    private String awayProbablePitcher;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "game_state")
    private GameState gameState;

    public Game(final Stadium stadium, final Team homeTeam, final Team awayTeam, final LocalDate date,
                final LocalTime startAt, final String gameCode,
                final Integer homeScore, final Integer awayScore, final ScoreBoard homeScoreBoard,
                final ScoreBoard awayScoreBoard,
                final String homePitcher, final String awayPitcher, final GameState newState) {
        this.stadium = stadium;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.startAt = startAt;
        this.gameCode = gameCode;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeScoreBoard = homeScoreBoard;
        this.awayScoreBoard = awayScoreBoard;
        this.homePitcher = homePitcher;
        this.awayPitcher = awayPitcher;
        updateGameState(newState);
    }

    public void updateGameState(final GameState newState) {
        if (newState == GameState.CANCELED) {
            this.gameState = GameState.CANCELED;
            log.info("Game canceled: gameCode={}", this.gameCode);
            return;
        }

        if (this.gameState != null && !this.gameState.canTransitionTo(newState)) {
            log.warn("Invalid state transition blocked in game center update: " +
                            "gameCode={}, current={}, attempted={}",
                    this.gameCode, this.gameState, newState);
            return;
        }
        this.gameState = newState;
    }

    public void update(
            final Stadium stadium, final Team homeTeam, final Team awayTeam,
            final LocalDate date, final LocalTime startAt, final String gameCode,
            final Integer homeScore, final Integer awayScore, final ScoreBoard homeScoreBoard,
            final ScoreBoard awayScoreBoard, final String homePitcher, final String awayPitcher,
            final GameState newState
    ) {
        updateDetails(
                stadium, homeTeam, awayTeam,
                date, startAt, gameCode,
                homeScore, awayScore,
                homeScoreBoard, awayScoreBoard,
                homePitcher, awayPitcher
        );
        updateGameState(newState);
    }

    /**
     * Admin 정합성 복구 시 Bronze 원본을 기준으로 전체 필드를 강제 동기화한다.
     */
    public void reconcile(
            final Stadium stadium, final Team homeTeam, final Team awayTeam,
            final LocalDate date, final LocalTime startAt, final String gameCode,
            final Integer homeScore, final Integer awayScore, final ScoreBoard homeScoreBoard,
            final ScoreBoard awayScoreBoard, final String homePitcher, final String awayPitcher,
            final GameState gameState
    ) {
        updateDetails(
                stadium, homeTeam, awayTeam,
                date, startAt, gameCode,
                homeScore, awayScore,
                homeScoreBoard, awayScoreBoard,
                homePitcher, awayPitcher
        );
        this.gameState = gameState;
    }

    private void updateDetails(
            final Stadium stadium, final Team homeTeam, final Team awayTeam,
            final LocalDate date, final LocalTime startAt, final String gameCode,
            final Integer homeScore, final Integer awayScore, final ScoreBoard homeScoreBoard,
            final ScoreBoard awayScoreBoard, final String homePitcher, final String awayPitcher
    ) {
        this.stadium = stadium;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.startAt = startAt;
        this.gameCode = gameCode;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeScoreBoard = homeScoreBoard;
        this.awayScoreBoard = awayScoreBoard;
        this.homePitcher = homePitcher;
        this.awayPitcher = awayPitcher;
    }

    public boolean hasTeam(final Team team) {
        return homeTeam.equals(team) || awayTeam.equals(team);
    }

    /**
     * 게임센터 크롤링 결과로 선발 예고 투수를 갱신한다.
     * Bronze/ETL을 거치지 않고 games 테이블에 직접 반영한다.
     */
    public void updateProbablePitchers(final String homeProbablePitcher, final String awayProbablePitcher) {
        this.homeProbablePitcher = homeProbablePitcher;
        this.awayProbablePitcher = awayProbablePitcher;
    }
}

package com.yagubogu.game.domain;

import com.yagubogu.game.dto.Pitchers;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    @AttributeOverrides({
            @AttributeOverride(name = "winningPitcher", column = @Column(name = "winning_pitcher")),
            @AttributeOverride(name = "losingPitcher", column = @Column(name = "losing_pitcher")),
            @AttributeOverride(name = "savePitcher", column = @Column(name = "save_pitcher")),
            @AttributeOverride(name = "holdPitcher", column = @Column(name = "hold_pitcher"))
    })
    @Embedded
    private Pitchers pitchers;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "game_state")
    private GameState gameState;

    public Game(final Stadium stadium, final Team homeTeam, final Team awayTeam, final LocalDate date,
                final LocalTime startAt, final String gameCode,
                final Integer homeScore, final Integer awayScore, final ScoreBoard homeScoreBoard,
                final ScoreBoard awayScoreBoard,
                final Pitchers pitchers,
                final GameState gameState) {
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
        this.pitchers = pitchers;
        this.gameState = gameState;
    }

    public void updateGameState(final GameState gameState) {
        this.gameState = gameState;
    }

    public void updateScoreBoard(
            final ScoreBoard homeScoreBoard,
            final ScoreBoard awayScoreBoard,
            final String winningPitcher,
            final String losingPitcher,
            final String savePitcher,
            final String holdPitcher
    ) {
        // TODO: homeScore과 homeScoreBoard의 runs가 중복되므로 homeScore 제거
        this.homeScore = homeScoreBoard.getRuns();
        this.awayScore = awayScoreBoard.getRuns();
        this.homeScoreBoard = homeScoreBoard;
        this.awayScoreBoard = awayScoreBoard;
        if (this.pitchers == null) {
            this.pitchers = new Pitchers(winningPitcher, losingPitcher, savePitcher, holdPitcher);
        } else {
            this.pitchers.update(winningPitcher, losingPitcher, savePitcher, holdPitcher);
        }
    }

    public boolean hasTeam(final Team team) {
        return homeTeam.equals(team) || awayTeam.equals(team);
    }
}

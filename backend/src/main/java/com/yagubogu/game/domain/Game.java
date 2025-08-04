package com.yagubogu.game.domain;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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


    public Game(
            final Stadium stadium,
            final Team homeTeam,
            final Team awayTeam,
            final LocalDate date,
            final LocalTime startAt,
            final String gameCode,
            final Integer homeScore,
            final Integer awayScore
    ) {
        this.stadium = stadium;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.startAt = startAt;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.gameCode = gameCode;
    }

    public boolean hasTeam(final Team team) {
        return homeTeam.equals(team) || awayTeam.equals(team);
    }
}

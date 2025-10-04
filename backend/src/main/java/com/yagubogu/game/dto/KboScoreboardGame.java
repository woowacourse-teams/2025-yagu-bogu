package com.yagubogu.game.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class KboScoreboardGame {

    private final LocalDate date;
    private final String gameId;
    private final String status;
    private final String stadium;
    private final String startTime;
    private final String boxScoreUrl;
    private final KboScoreboardTeam awayTeam;
    private final KboScoreboardTeam homeTeam;
    private final int awayScore;
    private final int homeScore;
    private final String winningPitcher;
    private final String savingPitcher;
    private final String losingPitcher;
    private int doubleHeaderGameOrder = -1;

    public KboScoreboardGame(
            LocalDate date,
            String gameId,
            String status,
            String stadium,
            String startTime,
            String boxScoreUrl,
            KboScoreboardTeam awayTeam,
            KboScoreboardTeam homeTeam,
            int awayScore,
            int homeScore,
            String winningPitcher,
            String savingPitcher,
            String losingPitcher
    ) {
        this.date = date;
        this.gameId = gameId;
        this.status = status;
        this.stadium = stadium;
        this.startTime = startTime;
        this.boxScoreUrl = boxScoreUrl;
        this.awayTeam = awayTeam;
        this.homeTeam = homeTeam;
        this.awayScore = awayScore;
        this.homeScore = homeScore;
        this.winningPitcher = winningPitcher;
        this.savingPitcher = savingPitcher;
        this.losingPitcher = losingPitcher;
    }
}

package com.yagubogu.game.service.crawler.KboScheduleCrawler;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class KboGame {

    private final LocalDate date;
    private final String gameTime;
    private final String homeTeam;
    private final Integer homeScore;
    private final String awayTeam;
    private final Integer awayScore;
    private final String result;
    private final String stadium;
    private final boolean cancelled;
    private final String cancelReason;
    private final ScheduleType scheduleType;
    private final String tvChannel;
    private int doubleHeaderGameOrder = -1;

    public KboGame(final LocalDate date,
                   final String gameTime,
                   final String homeTeam,
                   final String homeScore,
                   final String awayTeam,
                   final String awayScore,
                   final String result,
                   final String stadium,
                   final boolean cancelled,
                   final String cancelReason,
                   final ScheduleType scheduleType,
                   final String tvChannel) {
        this.date = date;
        this.gameTime = gameTime;
        this.homeTeam = homeTeam;
        this.homeScore = parseScore(homeScore);
        this.awayTeam = awayTeam;
        this.awayScore = parseScore(awayScore);
        this.result = result;
        this.stadium = stadium;
        this.cancelled = cancelled;
        this.cancelReason = cancelReason;
        this.scheduleType = scheduleType;
        this.tvChannel = tvChannel;
    }

    private Integer parseScore(final String score) {
        if (score == null) {
            return null;
        }
        if (score.equals("-")) {
            return 0;
        }
        return Integer.parseInt(score);
    }
}

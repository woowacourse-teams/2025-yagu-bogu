package com.yagubogu.game.service.crawler.KboScheduleCrawler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class KboGame {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LocalDate date;
    private final String gameTime;
    private final String homeTeam;
    private final String homeScore;
    private final String awayTeam;
    private final String awayScore;
    private final String result;
    private final String stadium;
    private final boolean cancelled;
    private final String cancelReason;
    private final String scheduleType;
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
                   final String scheduleType,
                   final String tvChannel) {
        this.date = date;
        this.gameTime = gameTime;
        this.homeTeam = homeTeam;
        this.homeScore = homeScore;
        this.awayTeam = awayTeam;
        this.awayScore = awayScore;
        this.result = result;
        this.stadium = stadium;
        this.cancelled = cancelled;
        this.cancelReason = cancelReason;
        this.scheduleType = scheduleType;
        this.tvChannel = tvChannel;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getGameTime() {
        return gameTime;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getHomeScore() {
        return homeScore;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public String getAwayScore() {
        return awayScore;
    }

    public String getResult() {
        return result;
    }

    public String getStadium() {
        return stadium;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public String getTvChannel() {
        return tvChannel;
    }

    public int getDoubleHeaderGameOrder() {
        return doubleHeaderGameOrder;
    }

    public void setDoubleHeaderGameOrder(final int doubleHeaderGameOrder) {
        this.doubleHeaderGameOrder = doubleHeaderGameOrder;
    }

    public Map<String, Object> toDocument() {
        Map<String, Object> document = new HashMap<>();
        document.put("date", DATE_FORMAT.format(date));
        document.put("team1", homeTeam);
        document.put("team1_score", homeScore);
        document.put("team2", awayTeam);
        document.put("team2_score", awayScore);
        document.put("result", result);
        document.put("stadium", stadium);
        document.put("cancel", cancelled);
        document.put("cancelReason", cancelReason);
        document.put("game_time", gameTime);
        document.put("schedule_type", scheduleType);
        document.put("doubleHeaderGameOrder", doubleHeaderGameOrder);
        document.put("tv", tvChannel);
        return document;
    }
}

package yagubogu.crawling.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameInfo {
    private String gameId;
    private String gameDate;
    private String gameSc;
    private String status;
    private String gameStatus;  // "END", "CANCEL", "SCHEDULED" ← 추가
    private String awayTeam;
    private String homeTeam;
    private String awayScore;
    private String homeScore;
    private String stadium;
    private String startTime;

    /**
     * 경기 상태 판별
     */
    public boolean isEnded() {
        return "END".equals(gameStatus);
    }

    public boolean isCanceled() {
        return "CANCEL".equals(gameStatus);
    }

    public boolean isScheduled() {
        return "SCHEDULED".equals(gameStatus);
    }

    @Override
    public String toString() {
        String scoreInfo = (awayScore != null && homeScore != null)
                ? String.format("%s vs %s", awayScore, homeScore)
                : "vs";

        return String.format("[%s %s] %s %s %s (%s) - %s [%s]",
                gameDate, startTime, awayTeam, scoreInfo, homeTeam,
                stadium, status, gameStatus);
    }
}

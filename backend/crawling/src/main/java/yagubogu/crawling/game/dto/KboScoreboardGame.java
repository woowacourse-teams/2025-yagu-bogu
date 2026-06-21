package yagubogu.crawling.game.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class KboScoreboardGame {

    private final LocalDate date;
    private final String status;
    private final String stadium;
    private final LocalTime startTime;
    private final String boxScoreUrl;
    private final KboScoreboardTeam awayTeamScoreboard;
    private final KboScoreboardTeam homeTeamScoreboard;
    private final Integer awayScore;
    private final Integer homeScore;
    private final String winningPitcher;
    private final String savingPitcher;
    private final String losingPitcher;

    // 진루정보 (경기중이 아니면 null)
    private Boolean firstBaseOccupied;
    private Boolean secondBaseOccupied;
    private Boolean thirdBaseOccupied;

    // 볼/스트라이크/아웃 카운트 (경기중이 아니면 null)
    private Integer balls;
    private Integer strikes;
    private Integer outs;

    public KboScoreboardGame(
            LocalDate date,
            String status,
            String stadium,
            LocalTime startTime,
            String boxScoreUrl,
            KboScoreboardTeam awayTeamScoreboard,
            KboScoreboardTeam homeTeamScoreboard,
            Integer awayScore,
            Integer homeScore,
            String winningPitcher,
            String savingPitcher,
            String losingPitcher
    ) {
        this.date = date;
        this.status = status;
        this.stadium = stadium;
        this.startTime = startTime;
        this.boxScoreUrl = boxScoreUrl;
        this.awayTeamScoreboard = awayTeamScoreboard;
        this.homeTeamScoreboard = homeTeamScoreboard;
        this.awayScore = awayScore;
        this.homeScore = homeScore;
        this.winningPitcher = winningPitcher;
        this.savingPitcher = savingPitcher;
        this.losingPitcher = losingPitcher;
    }
}

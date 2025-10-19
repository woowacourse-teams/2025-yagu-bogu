package yagubogu.crawling.game.dto;

import java.util.List;
import lombok.Data;

@Data
public class GameCenterDetail {

    // 기본 정보
    private String date;
    private String gameId;
    private String gameDate;
    private String gameSc;

    // 팀 정보
    private String awayTeamCode;
    private String homeTeamCode;
    private String awayTeamName;
    private String homeTeamName;

    // 경기장 정보
    private String stadium;
    private String stadiumName;
    private String weatherIcon;
    private String startTime;

    // 경기 상태
    private String gameStatus;  // 경기종료, 경기취소, 경기예정
    private String status;      // HTML에서 가져온 상태 텍스트
    private String broadcasting; // 중계 정보

    // 점수
    private String awayScore;
    private String homeScore;
    private String winner;  // "away" or "home"

    // 투수 정보
    private List<String> awayPitchers;
    private List<String> homePitchers;
}


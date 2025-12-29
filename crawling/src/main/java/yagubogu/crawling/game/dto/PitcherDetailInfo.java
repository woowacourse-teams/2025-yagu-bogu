package yagubogu.crawling.game.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PitcherDetailInfo {
    // 기본 정보
    private String date;              // 일자
    private String homeAway;          // 홈/어웨이
    private String teamName;          // 팀명
    private String pitcherName;       // 선발투수

    // 시즌 기록
    private String seasonEra;         // 시즌평균자책점
    private String seasonWar;         // 시즌WAR
    private String seasonGames;       // 시즌경기
    private String seasonAvgInning;   // 시즌선발평균이닝
    private String seasonQs;          // 시즌QS
    private String seasonWhip;        // 시즌WHIP

    // 홈/어웨이 기록
    private String haEra;             // 홈어웨이평균자책점
    private String haGames;           // 홈어웨이경기
    private String haAvgInning;       // 홈어웨이선발평균이닝
    private String haQs;              // 홈어웨이QS
    private String haWhip;            // 홈어웨이WHIP

    // 맞대결 기록
    private String vsEra;             // 맞대결평균자책점
    private String vsGames;           // 맞대결경기
    private String vsAvgInning;       // 맞대결선발평균이닝
    private String vsQs;              // 맞대결QS
    private String vsWhip;            // 맞대결WHIP
}

package yagubogu.crawling.game.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamDetailInfo {
    // 기본 정보
    private String date;              // 일자
    private String homeAway;          // 홈/어웨이
    private String teamName;          // 팀명

    // 시즌 기록
    private String seasonEra;         // 시즌평균자책점
    private String seasonAvg;         // 시즌타율
    private String seasonAvgScore;    // 시즌평균득점
    private String seasonAvgLost;     // 시즌평균실점

    // 홈/어웨이 기록
    private String haEra;             // 홈어웨이평균자책점
    private String haAvg;             // 홈어웨이시즌타율
    private String haAvgScore;        // 홈어웨이평균득점
    private String haAvgLost;         // 홈어웨이평균실점

    // 맞대결 기록
    private String vsEra;             // 맞대결평균자책점
    private String vsAvg;             // 맞대결타율
    private String vsAvgScore;        // 맞대결평균득점
    private String vsAvgLost;         // 맞대결평균실점
}


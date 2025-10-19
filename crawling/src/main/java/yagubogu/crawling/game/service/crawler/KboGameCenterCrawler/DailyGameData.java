package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.dto.PitcherDetailInfo;
import yagubogu.crawling.game.dto.TeamDetailInfo;

/**
 * 일일 경기 데이터 컨테이너
 */

@Slf4j
@Getter
@Setter
public class DailyGameData {

    private String date;
    private List<PitcherDetailInfo> pitchers = new ArrayList<>();
    private List<TeamDetailInfo> teams = new ArrayList<>();

    public void addPitchers(List<PitcherDetailInfo> pitchers) {
        this.pitchers.addAll(pitchers);
    }

    public void addTeams(List<TeamDetailInfo> teams) {
        this.teams.addAll(teams);
    }

    /**
     * CSV 저장 (디렉토리 자동 생성)
     */
    public void saveToCsv(String basePath) {
        LocalDate today = LocalDate.now();

        // 디렉토리 생성
        String pitcherDir = basePath + "/daily_pitcher_vs";
        String teamDir = basePath + "/daily_team_vs";

        createDirectoryIfNotExists(pitcherDir);
        createDirectoryIfNotExists(teamDir);

        // CSV 저장
        savePitchersCsv(pitcherDir + "/일일경기선발투수정보_" + today + ".csv");
        saveTeamsCsv(teamDir + "/일일경기팀정보_" + today + ".csv");
    }

    /**
     * 디렉토리 생성
     */
    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("디렉토리 생성: {}", path);
            } else {
                log.error("디렉토리 생성 실패: {}", path);
            }
        }
    }

    /**
     * 선발투수 CSV 저장
     */
    private void savePitchersCsv(String filePath) {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            // 헤더
            writer.write("일자,홈/어웨이,팀명,선발투수,시즌평균자책점,시즌WAR,시즌경기,시즌선발평균이닝,시즌QS,시즌WHIP," +
                    "홈어웨이평균자책점,홈어웨이경기,홈어웨이선발평균이닝,홈어웨이QS,홈어웨이WHIP," +
                    "맞대결평균자책점,맞대결경기,맞대결선발평균이닝,맞대결QS,맞대결WHIP\n");

            // 데이터
            for (PitcherDetailInfo pitcher : pitchers) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        nvl(pitcher.getDate()), nvl(pitcher.getHomeAway()), nvl(pitcher.getTeamName()),
                        nvl(pitcher.getPitcherName()),
                        nvl(pitcher.getSeasonEra()), nvl(pitcher.getSeasonWar()), nvl(pitcher.getSeasonGames()),
                        nvl(pitcher.getSeasonAvgInning()), nvl(pitcher.getSeasonQs()), nvl(pitcher.getSeasonWhip()),
                        nvl(pitcher.getHaEra()), nvl(pitcher.getHaGames()), nvl(pitcher.getHaAvgInning()),
                        nvl(pitcher.getHaQs()), nvl(pitcher.getHaWhip()),
                        nvl(pitcher.getVsEra()), nvl(pitcher.getVsGames()), nvl(pitcher.getVsAvgInning()),
                        nvl(pitcher.getVsQs()), nvl(pitcher.getVsWhip())));
            }

            log.info("선발투수 정보 CSV 저장 완료: {}", filePath);

        } catch (IOException e) {
            log.error("CSV 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * 팀 CSV 저장
     */
    private void saveTeamsCsv(String filePath) {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            // 헤더
            writer.write("일자,홈/어웨이,팀명,시즌평균자책점,시즌타율,시즌평균득점,시즌평균실점," +
                    "홈어웨이평균자책점,홈어웨이시즌타율,홈어웨이평균득점,홈어웨이평균실점," +
                    "맞대결평균자책점,맞대결타율,맞대결평균득점,맞대결평균실점\n");

            // 데이터
            for (TeamDetailInfo team : teams) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        nvl(team.getDate()), nvl(team.getHomeAway()), nvl(team.getTeamName()),
                        nvl(team.getSeasonEra()), nvl(team.getSeasonAvg()), nvl(team.getSeasonAvgScore()),
                        nvl(team.getSeasonAvgLost()),
                        nvl(team.getHaEra()), nvl(team.getHaAvg()), nvl(team.getHaAvgScore()),
                        nvl(team.getHaAvgLost()),
                        nvl(team.getVsEra()), nvl(team.getVsAvg()), nvl(team.getVsAvgScore()),
                        nvl(team.getVsAvgLost())));
            }

            log.info("팀 정보 CSV 저장 완료: {}", filePath);

        } catch (IOException e) {
            log.error("CSV 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * null 체크
     */
    private String nvl(String value) {
        return value == null ? "" : value;
    }
}


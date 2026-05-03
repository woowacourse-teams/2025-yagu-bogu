package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;
import yagubogu.crawling.game.service.crawler.KboHttpClient;

@Slf4j
public class KboGameCenterCrawler {

    private final KboHttpClient kboHttpClient;

    public KboGameCenterCrawler(final KboHttpClient kboHttpClient) {
        this.kboHttpClient = kboHttpClient;
    }

    /**
     * 일일 경기 상세 정보 크롤링
     */
    public GameCenter fetchDailyGameCenter(final LocalDate date) {
        log.info("[GAME_CENTER] HTTP 크롤링 시작: date={}", date);
        GameCenter dailyData = new GameCenter();
        dailyData.setDate(date.toString());

        try {
            JsonNode games = kboHttpClient.fetchGameList(date).path("game");
            if (!games.isArray() || games.isEmpty()) {
                log.info("오늘 경기가 없습니다.");
                return dailyData;
            }

            log.info("총 {}경기 정보 수집 시작", games.size());

            for (JsonNode game : games) {
                dailyData.addGameDetail(toGameCenterDetail(game, date));
            }

            log.info("[GAME_CENTER] HTTP 크롤링 완료: date={}, count={}", date, dailyData.getGames().size());

        } catch (Exception e) {
            log.error("일일 데이터 크롤링 실패", e);
        }

        return dailyData;
    }

    private GameCenterDetail toGameCenterDetail(final JsonNode game, final LocalDate date) {
        GameCenterDetail detail = new GameCenterDetail();
        detail.setDate(date.toString());
        detail.setGameCode(text(game, "G_ID"));
        detail.setGameDate(text(game, "G_DT"));
        detail.setGameSc(text(game, "GAME_STATE_SC"));
        detail.setAwayTeamCode(text(game, "AWAY_ID"));
        detail.setHomeTeamCode(text(game, "HOME_ID"));
        detail.setAwayTeamName(text(game, "AWAY_NM"));
        detail.setHomeTeamName(text(game, "HOME_NM"));
        detail.setStadium(text(game, "S_NM"));
        detail.setStadiumName(text(game, "S_NM"));
        detail.setStartTime(text(game, "G_TM"));
        detail.setGameStatus(toGameStatus(text(game, "GAME_STATE_SC")));
        detail.setStatus(toStatus(text(game, "GAME_STATE_SC"), text(game, "GAME_INN_NO"), text(game, "GAME_TB_SC_NM")));
        detail.setBroadcasting(text(game, "TV_IF"));
        detail.setAwayScore(text(game, "T_SCORE_CN"));
        detail.setHomeScore(text(game, "B_SCORE_CN"));
        detail.setWinner(resolveWinner(detail.getAwayScore(), detail.getHomeScore()));
        detail.setAwayPitchers(extractPitchers(game, "T"));
        detail.setHomePitchers(extractPitchers(game, "B"));
        return detail;
    }

    private List<String> extractPitchers(final JsonNode game, final String prefix) {
        List<String> pitchers = new ArrayList<>();
        addPitcher(pitchers, "선발", text(game, prefix + "_PIT_P_NM"));
        return pitchers;
    }

    private void addPitcher(final List<String> pitchers, final String label, final String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        pitchers.add(label + " : " + name.trim());
    }

    private String toGameStatus(final String gameState) {
        return switch (gameState) {
            case "3" -> "경기종료";
            case "4" -> "경기취소";
            case "2", "5" -> "경기중";
            default -> "경기예정";
        };
    }

    private String toStatus(final String gameState, final String inning, final String topBottom) {
        return switch (gameState) {
            case "3" -> "경기종료";
            case "4" -> "경기취소";
            case "2", "5" -> inning.isBlank() ? "중" : inning + "회" + topBottom;
            default -> "경기전";
        };
    }

    private String resolveWinner(final String awayScore, final String homeScore) {
        Integer away = parseInt(awayScore);
        Integer home = parseInt(homeScore);
        if (away == null || home == null || away.equals(home)) {
            return null;
        }
        return away > home ? "away" : "home";
    }

    private Integer parseInt(final String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String text(final JsonNode node, final String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return "";
        }
        return value.asText();
    }
}

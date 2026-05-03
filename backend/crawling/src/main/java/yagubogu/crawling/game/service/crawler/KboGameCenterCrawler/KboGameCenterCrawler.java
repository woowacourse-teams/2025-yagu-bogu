package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;
import yagubogu.crawling.game.service.crawler.KboHtmlClient;

@Slf4j
public class KboGameCenterCrawler {

    private final KboHtmlClient kboHtmlClient;

    public KboGameCenterCrawler(final KboHtmlClient kboHtmlClient) {
        this.kboHtmlClient = kboHtmlClient;
    }

    /**
     * 일일 경기 상세 정보 크롤링
     */
    public GameCenter fetchDailyGameCenter(final LocalDate date) {
        log.info("[GAME_CENTER] HTTP 크롤링 시작: date={}", date);
        GameCenter dailyData = new GameCenter();
        dailyData.setDate(date.toString());

        try {
            Document document = kboHtmlClient.fetchGameCenter(date);
            Elements games = document.select(".game-list-n > li.game-cont, .game-list-n > li");
            if (games.isEmpty()) {
                log.info("[GAME_CENTER] HTML 내 경기 목록 없음: date={}", date);
                return dailyData;
            }

            log.info("총 {}경기 정보 수집 시작", games.size());

            for (Element game : games) {
                dailyData.addGameDetail(toGameCenterDetail(game, date));
            }

            log.info("[GAME_CENTER] HTTP 크롤링 완료: date={}, count={}", date, dailyData.getGames().size());

        } catch (Exception e) {
            log.error("일일 데이터 크롤링 실패", e);
        }

        return dailyData;
    }

    private GameCenterDetail toGameCenterDetail(final Element game, final LocalDate date) {
        GameCenterDetail detail = new GameCenterDetail();
        detail.setDate(date.toString());
        detail.setGameCode(attr(game, "g_id"));
        detail.setGameDate(attr(game, "g_dt"));
        detail.setGameSc(attr(game, "game_sc"));
        detail.setAwayTeamCode(attr(game, "away_id"));
        detail.setHomeTeamCode(attr(game, "home_id"));
        detail.setAwayTeamName(attr(game, "away_nm"));
        detail.setHomeTeamName(attr(game, "home_nm"));
        detail.setStadium(attr(game, "s_nm"));
        detail.setStadiumName(firstText(game.select(".top > ul > li"), 0, attr(game, "s_nm")));
        detail.setStartTime(resolveStartTime(game));
        detail.setGameStatus(toGameStatus(attr(game, "game_sc"), game.className()));
        detail.setStatus(firstText(game.select(".middle .staus"), 0, detail.getGameStatus()));
        detail.setBroadcasting(firstText(game.select(".middle .broadcasting"), 0, ""));
        detail.setAwayScore(firstText(game.select(".team.away .score"), 0, ""));
        detail.setHomeScore(firstText(game.select(".team.home .score"), 0, ""));
        detail.setWinner(resolveWinner(detail.getAwayScore(), detail.getHomeScore()));
        detail.setAwayPitchers(extractPitchers(game, "T"));
        detail.setHomePitchers(extractPitchers(game, "B"));
        return detail;
    }

    private List<String> extractPitchers(final Element game, final String prefix) {
        List<String> pitchers = new ArrayList<>();
        String teamSelector = "T".equals(prefix) ? ".team.away" : ".team.home";
        for (Element pitcher : game.select(teamSelector + " .today-pitcher p")) {
            String name = pitcher.text().trim();
            if (!name.isBlank()) {
                pitchers.add(name);
            }
        }
        return pitchers;
    }

    private String toGameStatus(final String gameState, final String className) {
        if (className != null && className.contains("end")) {
            return "경기종료";
        }
        if (className != null && className.contains("cancel")) {
            return "경기취소";
        }
        return switch (gameState) {
            case "3" -> "경기종료";
            case "4" -> "경기취소";
            case "2", "5" -> "경기중";
            default -> "경기예정";
        };
    }

    private String resolveStartTime(final Element game) {
        Elements topItems = game.select(".top > ul > li");
        if (topItems.size() >= 3) {
            return topItems.get(topItems.size() - 1).text().trim();
        }
        return attr(game, "g_tm");
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

    private String attr(final Element element, final String attrName) {
        String value = element.attr(attrName);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String firstText(final Elements elements, final int index, final String defaultValue) {
        if (elements.size() <= index) {
            return defaultValue;
        }
        return elements.get(index).text().trim();
    }
}

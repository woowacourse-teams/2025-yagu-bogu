package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.KboScoreboardGame;

class KboScoreboardParserTest {

    private KboScoreboardParser parser;

    @BeforeEach
    void setUp() {
        parser = new KboScoreboardParser(properties());
    }

    @Nested
    @DisplayName("스코어보드 파싱 테스트")
    class ParseScoreboardTests {

        @Test
        @DisplayName("parse - 완전한 데이터 파싱 성공")
        void parse_CompleteData() {
            LocalDate date = LocalDate.of(2025, 10, 26);
            Document document = Jsoup.parse("""
                    <div class="smsScore">
                        <div class="flag"><span>경기종료</span></div>
                        <div class="place">잠실 <span>18:30</span></div>
                        <div class="leftTeam">
                            <strong class="teamT">KT</strong>
                            <div class="score"><span>3</span></div>
                        </div>
                        <div class="rightTeam">
                            <strong class="teamT">LG</strong>
                            <div class="score"><span>5</span></div>
                        </div>
                        <div class="btnSms">
                            <a href="/Schedule/GameCenter/Main.aspx?gameDate=20251026&amp;gameId=20251026LGKT0">리뷰</a>
                        </div>
                        <table class="tScore">
                            <tbody>
                                <tr><th>KT</th><td>1</td><td>0</td><td>2</td><td>3</td><td>8</td><td>0</td><td>2</td></tr>
                                <tr><th>LG</th><td>0</td><td>2</td><td>3</td><td>5</td><td>10</td><td>1</td><td>3</td></tr>
                            </tbody>
                        </table>
                        <div class="score_wrap">
                            <p class="win">
                                <span>승: 김광현</span>
                                <span>패: 엄상백</span>
                                <span>세: 고우석</span>
                            </p>
                        </div>
                    </div>
                    """);

            List<KboScoreboardGame> result = parser.parse(document, date);

            assertThat(result).hasSize(1);
            KboScoreboardGame game = result.get(0);
            assertThat(game.getDate()).isEqualTo(date);
            assertThat(game.getStatus()).isEqualTo("경기종료");
            assertThat(game.getStadium()).isEqualTo("잠실");
            assertThat(game.getStartTime()).isEqualTo(LocalTime.of(18, 30));
            assertThat(game.getAwayScore()).isEqualTo(3);
            assertThat(game.getHomeScore()).isEqualTo(5);
            assertThat(game.getBoxScoreUrl())
                    .isEqualTo("https://www.koreabaseball.com/Schedule/GameCenter/Main.aspx?gameDate=20251026&gameId=20251026LGKT0");
            assertThat(game.getAwayTeamScoreboard().name()).isEqualTo("KT");
            assertThat(game.getAwayTeamScoreboard().runs()).isEqualTo(3);
            assertThat(game.getAwayTeamScoreboard().hits()).isEqualTo(8);
            assertThat(game.getAwayTeamScoreboard().errors()).isZero();
            assertThat(game.getAwayTeamScoreboard().basesOnBalls()).isEqualTo(2);
            assertThat(game.getAwayTeamScoreboard().inningScores()).containsExactly("1", "0", "2");
            assertThat(game.getHomeTeamScoreboard().name()).isEqualTo("LG");
            assertThat(game.getHomeTeamScoreboard().runs()).isEqualTo(5);
            assertThat(game.getWinningPitcher()).isEqualTo("김광현");
            assertThat(game.getLosingPitcher()).isEqualTo("엄상백");
            assertThat(game.getSavingPitcher()).isEqualTo("고우석");
        }

        @Test
        @DisplayName("parse - 스코어보드가 없으면 빈 목록")
        void parse_NoScoreboard_ReturnsEmpty() {
            Document document = Jsoup.parse("<div></div>");

            List<KboScoreboardGame> result = parser.parse(document, LocalDate.of(2025, 10, 26));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("parseScoreboard - 팀 정보 없으면 빈 Optional")
        void parseScoreboard_NoTeamInfo_ReturnsEmpty() {
            Document document = Jsoup.parse("<div class='smsScore'></div>");

            var result = parser.parseScoreboard(document.selectFirst(".smsScore"), LocalDate.of(2025, 10, 26));

            assertThat(result).isEmpty();
        }
    }

    private KboCrawlerProperties properties() {
        KboCrawlerProperties properties = new KboCrawlerProperties();
        properties.setCrawler(crawlerConfig());
        properties.setSelectors(selectors());
        properties.setPatterns(patterns());
        return properties;
    }

    private KboCrawlerProperties.CrawlerConfig crawlerConfig() {
        KboCrawlerProperties.CrawlerConfig config = new KboCrawlerProperties.CrawlerConfig();
        config.setBaseUrl("https://www.koreabaseball.com");
        config.setScoreBoardUrl("https://www.koreabaseball.com/Schedule/ScoreBoard.aspx");
        config.setNavigationTimeout(Duration.ofSeconds(10));
        config.setWaitTimeout(Duration.ofSeconds(5));
        return config;
    }

    private KboCrawlerProperties.Selectors selectors() {
        KboCrawlerProperties.Selectors selectors = new KboCrawlerProperties.Selectors();
        selectors.setScoreboard(scoreboardSelectors());
        return selectors;
    }

    private KboCrawlerProperties.ScoreboardSelectors scoreboardSelectors() {
        KboCrawlerProperties.ScoreboardSelectors selectors = new KboCrawlerProperties.ScoreboardSelectors();
        selectors.setContainer(".smsScore");
        selectors.setStatus(".flag span");
        selectors.setStadium(".place");
        selectors.setStartTime(".place span");
        selectors.setAwayTeam(teamSelectors(".leftTeam .teamT", ".leftTeam .score span"));
        selectors.setHomeTeam(teamSelectors(".rightTeam .teamT", ".rightTeam .score span"));
        selectors.setBoxScoreLink(".btnSms a[href*='gameId=']");
        selectors.setScoreTable(scoreTableSelectors());
        selectors.setPitcher(pitcherSelectors());
        return selectors;
    }

    private KboCrawlerProperties.TeamSelectors teamSelectors(final String name, final String score) {
        KboCrawlerProperties.TeamSelectors selectors = new KboCrawlerProperties.TeamSelectors();
        selectors.setName(name);
        selectors.setScore(score);
        return selectors;
    }

    private KboCrawlerProperties.ScoreTableSelectors scoreTableSelectors() {
        KboCrawlerProperties.ScoreTableSelectors selectors = new KboCrawlerProperties.ScoreTableSelectors();
        selectors.setTable("table.tScore");
        selectors.setRows("tbody tr");
        selectors.setTeamName("th");
        selectors.setCells("td");
        return selectors;
    }

    private KboCrawlerProperties.PitcherSelectors pitcherSelectors() {
        KboCrawlerProperties.PitcherSelectors selectors = new KboCrawlerProperties.PitcherSelectors();
        selectors.setContainer(".score_wrap p.win");
        selectors.setSpans("span");
        return selectors;
    }

    private KboCrawlerProperties.Patterns patterns() {
        KboCrawlerProperties.Patterns patterns = new KboCrawlerProperties.Patterns();
        patterns.setPitcherLabel("^\\s*(승|세|패)\\s*[:：]\\s*(.+?)\\s*$");
        patterns.setTimeFormat("HH:mm");
        return patterns;
    }
}

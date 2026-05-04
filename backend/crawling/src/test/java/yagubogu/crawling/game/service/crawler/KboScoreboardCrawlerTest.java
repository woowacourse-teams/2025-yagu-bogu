package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardParser;

@ExtendWith(MockitoExtension.class)
class KboScoreboardCrawlerTest {

    @Mock
    private KboHtmlClient kboHtmlClient;

    @Mock
    private KboScoreboardParser scoreboardParser;

    private KboScoreboardCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new KboScoreboardCrawler(kboHtmlClient, scoreboardParser, Duration.ZERO);
    }

    @Nested
    @DisplayName("범위 크롤링 테스트")
    class RangeCrawlingTests {

        @Test
        @DisplayName("crawl - 단일 날짜 크롤링 성공")
        void crawl_SingleDate_Success() {
            LocalDate date = LocalDate.of(2025, 10, 26);
            Document document = Jsoup.parse("<div class='smsScore'></div>");
            List<KboScoreboardGame> games = List.of(game(date));

            when(kboHtmlClient.fetchScoreboard(date)).thenReturn(document);
            when(scoreboardParser.parse(document, date)).thenReturn(games);

            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(List.of(date));

            assertThat(result).containsOnlyKeys(date);
            assertThat(result.get(date)).containsExactlyElementsOf(games);
        }

        @Test
        @DisplayName("crawl - 여러 날짜 크롤링 성공")
        void crawl_MultipleDates_Success() {
            LocalDate first = LocalDate.of(2025, 10, 24);
            LocalDate second = LocalDate.of(2025, 10, 25);
            Document firstDocument = Jsoup.parse("<div class='smsScore'></div>");
            Document secondDocument = Jsoup.parse("<div class='smsScore'></div>");

            when(kboHtmlClient.fetchScoreboard(first)).thenReturn(firstDocument);
            when(kboHtmlClient.fetchScoreboard(second)).thenReturn(secondDocument);
            when(scoreboardParser.parse(firstDocument, first)).thenReturn(List.of(game(first)));
            when(scoreboardParser.parse(secondDocument, second)).thenReturn(List.of(game(second), game(second)));

            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(List.of(first, second));

            assertThat(result).hasSize(2);
            assertThat(result.get(first)).hasSize(1);
            assertThat(result.get(second)).hasSize(2);
        }

        @Test
        @DisplayName("crawl - 빈 날짜 리스트")
        void crawl_EmptyDateList_ReturnsEmpty() {
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("crawl - null 날짜 리스트")
        void crawl_NullDateList_ReturnsEmpty() {
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("crawl - 크롤링 실패 시 재시도")
        void crawl_RetryOnFailure() {
            LocalDate date = LocalDate.of(2025, 10, 26);
            Document document = Jsoup.parse("<div class='smsScore'></div>");
            List<KboScoreboardGame> games = List.of(game(date));

            when(kboHtmlClient.fetchScoreboard(date))
                    .thenThrow(new IllegalStateException("Timeout"))
                    .thenThrow(new IllegalStateException("Timeout"))
                    .thenReturn(document);
            when(scoreboardParser.parse(document, date)).thenReturn(games);

            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(List.of(date));

            assertThat(result).containsOnlyKeys(date);
            assertThat(result.get(date)).hasSize(1);
            verify(kboHtmlClient, times(3)).fetchScoreboard(date);
        }

        @Test
        @DisplayName("crawl - 최대 재시도 초과 시 실패")
        void crawl_MaxRetriesExceeded_Fails() {
            LocalDate date = LocalDate.of(2025, 10, 26);

            when(kboHtmlClient.fetchScoreboard(date))
                    .thenThrow(new IllegalStateException("Timeout"));

            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(List.of(date));

            assertThat(result).isEmpty();
            verify(kboHtmlClient, times(3)).fetchScoreboard(date);
        }
    }

    private KboScoreboardGame game(final LocalDate date) {
        KboScoreboardTeam awayTeam = new KboScoreboardTeam("KT", 3, 8, 0, 2, List.of());
        KboScoreboardTeam homeTeam = new KboScoreboardTeam("LG", 5, 10, 1, 3, List.of());
        return new KboScoreboardGame(
                date,
                "경기종료",
                "잠실",
                LocalTime.of(18, 30),
                null,
                awayTeam,
                homeTeam,
                3,
                5,
                null,
                null,
                null
        );
    }
}

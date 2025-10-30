package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;

@ExtendWith(MockitoExtension.class)
class KboScoreboardCrawlerTest {

    @Mock
    private KboCrawlerProperties mockProperties;

    @Mock
    private PlaywrightManager mockPlaywrightManager;

    @Mock
    private KboCrawlerProperties.CrawlerConfig mockCrawlerConfig;

    @Mock
    private KboCrawlerProperties.Selectors mockSelectors;

    @Mock
    private KboCrawlerProperties.ScoreboardSelectors mockScoreboardSelectors;

    @Mock
    private KboCrawlerProperties.CalendarSelectors mockCalendarSelectors;

    @Mock
    private KboCrawlerProperties.Patterns mockPatterns;

    private KboScoreboardCrawler crawler;

    @Mock
    private KboCrawlerProperties.ScoreTableSelectors mockScoreTableSelectors;

    @Mock
    private KboCrawlerProperties.PitcherSelectors mockPitcherSelectors;

    @BeforeEach
    void setUp() {
        lenient().when(mockProperties.getCrawler()).thenReturn(mockCrawlerConfig);
        lenient().when(mockProperties.getSelectors()).thenReturn(mockSelectors);
        lenient().when(mockProperties.getPatterns()).thenReturn(mockPatterns);

        // CrawlerConfig 설정
        lenient().when(mockCrawlerConfig.getScoreBoardUrl())
                .thenReturn("https://www.koreabaseball.com/scoreboard");
        lenient().when(mockCrawlerConfig.getNavigationTimeout())
                .thenReturn(Duration.ofSeconds(30));
        lenient().when(mockCrawlerConfig.getWaitTimeout())
                .thenReturn(Duration.ofSeconds(5));

        // Selectors 설정
        lenient().when(mockSelectors.getScoreboard()).thenReturn(mockScoreboardSelectors);
        lenient().when(mockSelectors.getCalendar()).thenReturn(mockCalendarSelectors);

        //  TeamSelectors Mock 추가
        KboCrawlerProperties.TeamSelectors mockHomeTeamSelectors = mock(KboCrawlerProperties.TeamSelectors.class);
        KboCrawlerProperties.TeamSelectors mockAwayTeamSelectors = mock(KboCrawlerProperties.TeamSelectors.class);

        // ScoreboardSelectors 설정
        lenient().when(mockScoreboardSelectors.getContainer()).thenReturn(".scoreboard-container");
        lenient().when(mockScoreboardSelectors.getStatus()).thenReturn(".status");
        lenient().when(mockScoreboardSelectors.getStadium()).thenReturn(".stadium");
        lenient().when(mockScoreboardSelectors.getStartTime()).thenReturn(".start-time");
        lenient().when(mockScoreboardSelectors.getBoxScoreLink()).thenReturn("a.box-score");

        //  HomeTeam, AwayTeam 설정
        // ScoreboardSelectors 하위 객체 연결
        lenient().when(mockScoreboardSelectors.getHomeTeam()).thenReturn(mockHomeTeamSelectors);
        lenient().when(mockScoreboardSelectors.getAwayTeam()).thenReturn(mockAwayTeamSelectors);
        lenient().when(mockScoreboardSelectors.getScoreTable()).thenReturn(mockScoreTableSelectors);
        lenient().when(mockScoreboardSelectors.getPitcher()).thenReturn(mockPitcherSelectors);

        //  TeamSelectors 내부 설정
        lenient().when(mockHomeTeamSelectors.getName()).thenReturn(".home-team .name");
        lenient().when(mockHomeTeamSelectors.getScore()).thenReturn(".home-team .score");
        lenient().when(mockAwayTeamSelectors.getName()).thenReturn(".away-team .name");
        lenient().when(mockAwayTeamSelectors.getScore()).thenReturn(".away-team .score");

        // CalendarSelectors 설정
        lenient().when(mockCalendarSelectors.getTrigger()).thenReturn("//div[@class='calendar-trigger']");
        lenient().when(mockCalendarSelectors.getContainer()).thenReturn("//div[@class='calendar-container']");
        lenient().when(mockCalendarSelectors.getYearSelect()).thenReturn("//select[@id='year']");
        lenient().when(mockCalendarSelectors.getMonthSelect()).thenReturn("//select[@id='month']");
        lenient().when(mockCalendarSelectors.getDayLink()).thenReturn("//a[text()='%s']");
        lenient().when(mockCalendarSelectors.getDateLabel()).thenReturn("//span[@id='date-label']");
        lenient().when(mockCalendarSelectors.getUpdatePanel()).thenReturn("#update-panel");

        // Patterns 설정
        lenient().when(mockPatterns.getDateFormat()).thenReturn("yyyy.MM.dd");
        lenient().when(mockPatterns.getTimeFormat()).thenReturn("HH:mm");
        lenient().when(mockPatterns.getPitcherLabel()).thenReturn("(승|패|세)\\s+(.+)");

        crawler = new KboScoreboardCrawler(mockProperties, mockPlaywrightManager);
    }

    @Nested
    @DisplayName("범위 크롤링 테스트")
    class RangeCrawlingTests {

        @Test
        @DisplayName("crawl - 단일 날짜 크롤링 성공")
        void crawl_SingleDate_Success() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);
            List<LocalDate> dates = List.of(date);

            Page mockPage = mock(Page.class);

            when(mockPlaywrightManager.withPage(any())).thenAnswer(invocation -> {
                Function<Page, List<KboScoreboardGame>> function = invocation.getArgument(0);

                setupScoreboardPageMocks(mockPage, 3);

                return function.apply(mockPage);
            });

            // When
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(dates);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(date);
            assertThat(result.get(date)).hasSize(3);
        }

        @Test
        @DisplayName("crawl - 여러 날짜 크롤링 성공")
        void crawl_MultipleDates_Success() {
            // Given
            List<LocalDate> dates = List.of(
                    LocalDate.of(2025, 10, 24),
                    LocalDate.of(2025, 10, 25),
                    LocalDate.of(2025, 10, 26)
            );

            Page mockPage = mock(Page.class);

            when(mockPlaywrightManager.withPage(any())).thenAnswer(invocation -> {
                Function<Page, List<KboScoreboardGame>> function = invocation.getArgument(0);

                setupScoreboardPageMocks(mockPage, 2);

                return function.apply(mockPage);
            });

            // When
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(dates);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(dates.get(0))).hasSize(2);
            assertThat(result.get(dates.get(1))).hasSize(2);
            assertThat(result.get(dates.get(2))).hasSize(2);
        }

        @Test
        @DisplayName("crawl - 빈 날짜 리스트")
        void crawl_EmptyDateList_ReturnsEmpty() {
            // When
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(List.of());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("crawl - null 날짜 리스트")
        void crawl_NullDateList_ReturnsEmpty() {
            // When
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("crawl - 크롤링 실패 시 재시도")
        void crawl_RetryOnFailure() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);
            List<LocalDate> dates = List.of(date);

            Page mockPage = mock(Page.class);

            // 첫 2번 실패, 3번째 성공
            when(mockPlaywrightManager.withPage(any()))
                    .thenThrow(new PlaywrightException("Timeout"))
                    .thenThrow(new PlaywrightException("Timeout"))
                    .thenAnswer(invocation -> {
                        Function<Page, List<KboScoreboardGame>> function = invocation.getArgument(0);
                        setupScoreboardPageMocks(mockPage, 1);
                        return function.apply(mockPage);
                    });

            // When
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(dates);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(date)).hasSize(1);
            verify(mockPlaywrightManager, times(3)).withPage(any());
        }

        @Test
        @DisplayName("crawl - 최대 재시도 초과 시 실패")
        void crawl_MaxRetriesExceeded_Fails() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);
            List<LocalDate> dates = List.of(date);

            // 모든 시도 실패
            when(mockPlaywrightManager.withPage(any()))
                    .thenThrow(new PlaywrightException("Timeout"));

            // When
            Map<LocalDate, List<KboScoreboardGame>> result = crawler.crawl(dates);

            // Then
            assertThat(result).isEmpty();
            verify(mockPlaywrightManager, times(3)).withPage(any());
        }
    }

    // ==================== 헬퍼 메서드 ====================

    private void setupScoreboardPageMocks(Page mockPage, int scoreboardCount) {
        //  Navigate
        lenient().when(mockPage.navigate(anyString(), any(Page.NavigateOptions.class)))
                .thenReturn(null);

        //  Navigation Mocks (달력 조작)
        setupNavigationMocks(mockPage);

        //  hasScoreboards
        lenient().when(mockPage.waitForSelector(
                eq(".scoreboard-container"),
                any(Page.WaitForSelectorOptions.class)
        )).thenReturn(null);

        //  getScoreboards
        List<ElementHandle> scoreboards = new ArrayList<>();
        for (int i = 0; i < scoreboardCount; i++) {
            scoreboards.add(createMockScoreboardElement());
        }
        lenient().when(mockPage.querySelectorAll(".scoreboard-container")).thenReturn(scoreboards);
    }

    private void setupNavigationMocks(Page mockPage) {
        // Calendar Trigger
        Locator triggerLocator = mock(Locator.class);
        lenient().when(mockPage.locator(contains("calendar-trigger"))).thenReturn(triggerLocator);

        // Calendar Container
        Locator containerLocator = mock(Locator.class);
        lenient().when(mockPage.locator(contains("calendar-container"))).thenReturn(containerLocator);

        // Year/Month Select
        Locator yearLocator = mock(Locator.class);
        Locator monthLocator = mock(Locator.class);
        lenient().when(mockPage.locator(contains("year"))).thenReturn(yearLocator);
        lenient().when(mockPage.locator(contains("month"))).thenReturn(monthLocator);
        lenient().when(yearLocator.selectOption(anyString())).thenReturn(List.of());
        lenient().when(monthLocator.selectOption(anyString())).thenReturn(List.of());

        // Day Click
        Locator dayLocator = mock(Locator.class);
        lenient().when(mockPage.locator(anyString())).thenReturn(dayLocator);
        lenient().when(dayLocator.count()).thenReturn(1);
        lenient().when(dayLocator.first()).thenReturn(dayLocator);

        //  Date Label (for needsDateChangeValidation)
        Locator dateLabelLocator = mock(Locator.class);
        Locator filteredLocator = mock(Locator.class);

        lenient().when(mockPage.locator(contains("date-label"))).thenReturn(dateLabelLocator);
        lenient().when(dateLabelLocator.filter(any(Locator.FilterOptions.class)))
                .thenReturn(filteredLocator);

        // Update Panel
        Locator updatePanelLocator = mock(Locator.class);
        lenient().when(mockPage.locator(contains("update-panel"))).thenReturn(updatePanelLocator);
    }

    private ElementHandle createMockScoreboardElement() {
        ElementHandle scoreboard = mock(ElementHandle.class);

        //  기본 정보
        ElementHandle statusElem = createMockElement("경기종료");
        ElementHandle stadiumElem = createMockElement("잠실");
        ElementHandle startTimeElem = createMockElement("18:30");

        lenient().when(scoreboard.querySelector(".status")).thenReturn(statusElem);
        lenient().when(scoreboard.querySelector(".stadium")).thenReturn(stadiumElem);
        lenient().when(scoreboard.querySelector(".start-time")).thenReturn(startTimeElem);

        //  Away 팀 정보
        ElementHandle awayNameElem = createMockElement("KT");
        ElementHandle awayScoreElem = createMockElement("3");

        lenient().when(scoreboard.querySelector(".away-team .name")).thenReturn(awayNameElem);
        lenient().when(scoreboard.querySelector(".away-team .score")).thenReturn(awayScoreElem);

        //  Home 팀 정보
        ElementHandle homeNameElem = createMockElement("LG");
        ElementHandle homeScoreElem = createMockElement("5");

        lenient().when(scoreboard.querySelector(".home-team .name")).thenReturn(homeNameElem);
        lenient().when(scoreboard.querySelector(".home-team .score")).thenReturn(homeScoreElem);

        //  BoxScore Link
        ElementHandle boxScoreLink = mock(ElementHandle.class);
        lenient().when(boxScoreLink.getAttribute("href")).thenReturn("/game/boxscore/20251026LGKT");
        lenient().when(scoreboard.querySelector("a.box-score")).thenReturn(boxScoreLink);

        //  스코어 테이블 (완전한 Mock)
        ElementHandle mockTable = createCompleteScoreTable();
        lenient().when(scoreboard.querySelector("table.score-table")).thenReturn(mockTable);

        //  Pitcher 정보
        ElementHandle pitcherContainer = mock(ElementHandle.class);
        lenient().when(scoreboard.querySelector(".pitcher-info")).thenReturn(pitcherContainer);

        ElementHandle winPitcher = createMockElement("승 김광현");
        ElementHandle losePitcher = createMockElement("패 엄상백");
        ElementHandle savePitcher = createMockElement("세 고우석");

        lenient().when(pitcherContainer.querySelectorAll("span"))
                .thenReturn(List.of(winPitcher, losePitcher, savePitcher));

        return scoreboard;
    }

    private ElementHandle createCompleteScoreTable() {
        ElementHandle mockTable = mock(ElementHandle.class);

        //  LG 팀 행
        ElementHandle lgRow = mock(ElementHandle.class);
        ElementHandle lgTeamName = createMockElement("LG");
        ElementHandle lgScore1 = createMockElement("0");
        ElementHandle lgScore2 = createMockElement("2");
        ElementHandle lgScore3 = createMockElement("0");
        ElementHandle lgScore4 = createMockElement("1");
        ElementHandle lgScore5 = createMockElement("2");
        ElementHandle lgScore6 = createMockElement("0");
        ElementHandle lgScore7 = createMockElement("0");
        ElementHandle lgScore8 = createMockElement("0");
        ElementHandle lgScore9 = createMockElement("0");
        ElementHandle lgRuns = createMockElement("5");
        ElementHandle lgHits = createMockElement("10");
        ElementHandle lgErrors = createMockElement("1");
        ElementHandle lgBalls = createMockElement("3");

        lenient().when(lgRow.querySelector("td.team-name")).thenReturn(lgTeamName);
        lenient().when(lgRow.querySelectorAll("td")).thenReturn(
                List.of(lgTeamName, lgScore1, lgScore2, lgScore3, lgScore4,
                        lgScore5, lgScore6, lgScore7, lgScore8, lgScore9,
                        lgRuns, lgHits, lgErrors, lgBalls)
        );

        //  KT 팀 행
        ElementHandle ktRow = mock(ElementHandle.class);
        ElementHandle ktTeamName = createMockElement("KT");
        ElementHandle ktScore1 = createMockElement("1");
        ElementHandle ktScore2 = createMockElement("0");
        ElementHandle ktScore3 = createMockElement("2");
        ElementHandle ktScore4 = createMockElement("0");
        ElementHandle ktScore5 = createMockElement("0");
        ElementHandle ktScore6 = createMockElement("0");
        ElementHandle ktScore7 = createMockElement("0");
        ElementHandle ktScore8 = createMockElement("0");
        ElementHandle ktScore9 = createMockElement("0");
        ElementHandle ktRuns = createMockElement("3");
        ElementHandle ktHits = createMockElement("8");
        ElementHandle ktErrors = createMockElement("0");
        ElementHandle ktBalls = createMockElement("2");

        lenient().when(ktRow.querySelector("td.team-name")).thenReturn(ktTeamName);
        lenient().when(ktRow.querySelectorAll("td")).thenReturn(
                List.of(ktTeamName, ktScore1, ktScore2, ktScore3, ktScore4,
                        ktScore5, ktScore6, ktScore7, ktScore8, ktScore9,
                        ktRuns, ktHits, ktErrors, ktBalls)
        );

        lenient().when(mockTable.querySelectorAll("tr")).thenReturn(List.of(lgRow, ktRow));

        return mockTable;
    }

    private ElementHandle createMockElement(String text) {
        ElementHandle elem = mock(ElementHandle.class);
        lenient().when(elem.innerText()).thenReturn(text);
        lenient().when(elem.textContent()).thenReturn(text);
        return elem;
    }
}

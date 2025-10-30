package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
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
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;

@ExtendWith(MockitoExtension.class)
class KboGameCenterCrawlerTest {

    @Mock
    private KboCrawlerProperties mockProperties;

    @Mock
    private PlaywrightManager mockPlaywrightManager;

    @Mock
    private KboCrawlerProperties.CrawlerConfig mockCrawlerConfig;

    @Mock
    private KboCrawlerProperties.Selectors mockSelectors;

    @Mock
    private KboCrawlerProperties.CalendarSelectors mockCalendarSelectors;

    @Mock
    private KboCrawlerProperties.Patterns mockPatterns;

    private KboGameCenterCrawler crawler;

    @BeforeEach
    void setUp() {
        // Properties 설정
        lenient().when(mockProperties.getCrawler()).thenReturn(mockCrawlerConfig);
        lenient().when(mockProperties.getSelectors()).thenReturn(mockSelectors);
        lenient().when(mockProperties.getPatterns()).thenReturn(mockPatterns);

        lenient().when(mockCrawlerConfig.getGameCenterUrl())
                .thenReturn("https://www.koreabaseball.com/gameCenter");
        lenient().when(mockCrawlerConfig.getNavigationTimeout())
                .thenReturn(Duration.ofSeconds(30));
        lenient().when(mockCrawlerConfig.getWaitTimeout())
                .thenReturn(Duration.ofSeconds(5));

        // Selectors 설정
        lenient().when(mockSelectors.getCalendar()).thenReturn(mockCalendarSelectors);

        // CalendarSelectors 설정
        lenient().when(mockCalendarSelectors.getTrigger()).thenReturn("//div[@class='calendar-trigger']");
        lenient().when(mockCalendarSelectors.getContainer()).thenReturn("//div[@class='calendar-container']");
        lenient().when(mockCalendarSelectors.getYearSelect()).thenReturn("//select[@id='year']");
        lenient().when(mockCalendarSelectors.getMonthSelect()).thenReturn("//select[@id='month']");
        lenient().when(mockCalendarSelectors.getDayLink()).thenReturn("//a[text()='%s']");

        // Patterns 설정
        lenient().when(mockPatterns.getDateFormat()).thenReturn("yyyy.MM.dd");

        crawler = new KboGameCenterCrawler(mockProperties, mockPlaywrightManager);
    }

    @Nested
    @DisplayName("일일 크롤링 테스트")
    class DailyCrawlingTests {

        @Test
        @DisplayName("fetchDailyGameCenter - 정상 크롤링 성공")
        void fetchDailyGameCenter_Success() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);
            Page mockPage = mock(Page.class);

            when(mockPlaywrightManager.withPage(any())).thenAnswer(invocation -> {
                Function<Page, GameCenter> function = invocation.getArgument(0);

                setupSuccessfulPageMocks(mockPage, 5);

                return function.apply(mockPage);
            });

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo("2025-10-26");
            assertThat(result.getGames()).hasSize(5);
        }

        @Test
        @DisplayName("fetchDailyGameCenter - 경기 없음")
        void fetchDailyGameCenter_NoGames() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);
            Page mockPage = mock(Page.class);

            when(mockPlaywrightManager.withPage(any())).thenAnswer(invocation -> {
                Function<Page, GameCenter> function = invocation.getArgument(0);

                setupSuccessfulPageMocks(mockPage, 0);

                return function.apply(mockPage);
            });

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo("2025-10-26");
            assertThat(result.getGames()).isEmpty();
        }

        @Test
        @DisplayName("fetchDailyGameCenter - 예외 발생 시 빈 결과")
        void fetchDailyGameCenter_ExceptionReturnsEmpty() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);
            Page mockPage = mock(Page.class);

            when(mockPlaywrightManager.withPage(any())).thenAnswer(invocation -> {
                Function<Page, GameCenter> function = invocation.getArgument(0);

                lenient().when(mockPage.locator(anyString())).thenThrow(new PlaywrightException("Network error"));

                return function.apply(mockPage);
            });

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGames()).isEmpty();
        }

        @Test
        @DisplayName("fetchDailyGameCenter - PlaywrightManager 예외 처리")
        void fetchDailyGameCenter_PlaywrightManagerException() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 26);

            when(mockPlaywrightManager.withPage(any()))
                    .thenThrow(new RuntimeException("Browser crash"));

            // When & Then
            assertThatThrownBy(() -> crawler.fetchDailyGameCenter(date))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Browser crash");
        }
    }

    // ==================== 헬퍼 메서드 ====================

    private void setupSuccessfulPageMocks(Page mockPage, int gameCount) {
        // Navigate Mock
        lenient().when(mockPage.navigate(anyString(), any(Page.NavigateOptions.class)))
                .thenReturn(null);

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
        lenient().when(mockPage.locator(contains("26"))).thenReturn(dayLocator);
        lenient().when(dayLocator.count()).thenReturn(1);
        lenient().when(dayLocator.first()).thenReturn(dayLocator);

        // Date Label
        Locator dateLabelLocator = mock(Locator.class);
        lenient().when(mockPage.locator("#lblGameDate")).thenReturn(dateLabelLocator);
        lenient().when(dateLabelLocator.textContent()).thenReturn("2025.10.26 (일)");

        // Game List
        Locator gameListLocator = mock(Locator.class);
        lenient().when(mockPage.locator(".game-list-n > li.game-cont")).thenReturn(gameListLocator);
        lenient().when(gameListLocator.count()).thenReturn(gameCount);

        // waitForSelector for game list
        try {
            lenient().when(mockPage.waitForSelector(
                    eq(".game-list-n > li"),
                    any(Page.WaitForSelectorOptions.class)
            )).thenReturn(null);
        } catch (Exception e) {
            // ignore
        }

        // 각 경기 요소
        for (int i = 0; i < gameCount; i++) {
            Locator gameElement = createMockGameElement(i);
            lenient().when(gameListLocator.nth(i)).thenReturn(gameElement);
        }
    }

    private Locator createMockGameElement(int index) {
        Locator gameElement = mock(Locator.class);

        // 기본 속성
        lenient().when(gameElement.getAttribute("g_id")).thenReturn("GAME" + index);
        lenient().when(gameElement.getAttribute("g_dt")).thenReturn("20251026");
        lenient().when(gameElement.getAttribute("game_sc")).thenReturn(String.valueOf(index));
        lenient().when(gameElement.getAttribute("home_id")).thenReturn("LG");
        lenient().when(gameElement.getAttribute("away_id")).thenReturn("KT");
        lenient().when(gameElement.getAttribute("home_nm")).thenReturn("LG");
        lenient().when(gameElement.getAttribute("away_nm")).thenReturn("KT");
        lenient().when(gameElement.getAttribute("s_nm")).thenReturn("잠실");
        lenient().when(gameElement.getAttribute("class")).thenReturn("game-cont end");

        // Top 섹션
        Locator topItems = mock(Locator.class);
        Locator stadiumItem = mock(Locator.class);
        Locator weatherItem = mock(Locator.class);
        Locator timeItem = mock(Locator.class);

        lenient().when(gameElement.locator(".top > ul > li")).thenReturn(topItems);
        lenient().when(topItems.count()).thenReturn(3);
        lenient().when(topItems.nth(0)).thenReturn(stadiumItem);
        lenient().when(topItems.nth(1)).thenReturn(weatherItem);
        lenient().when(topItems.nth(2)).thenReturn(timeItem);

        lenient().when(stadiumItem.textContent()).thenReturn("잠실");
        lenient().when(timeItem.textContent()).thenReturn("18:30");

        // Weather image
        Locator weatherImg = mock(Locator.class);
        lenient().when(weatherItem.locator("img")).thenReturn(weatherImg);
        lenient().when(weatherImg.count()).thenReturn(1);
        lenient().when(weatherImg.getAttribute("src")).thenReturn("/images/weather/sunny.png");

        // Middle 섹션
        Locator broadcastingElem = mock(Locator.class);
        Locator statusElem = mock(Locator.class);

        lenient().when(gameElement.locator(".middle .broadcasting")).thenReturn(broadcastingElem);
        lenient().when(broadcastingElem.count()).thenReturn(1);
        lenient().when(broadcastingElem.textContent()).thenReturn("SPOTV");

        lenient().when(gameElement.locator(".middle .staus")).thenReturn(statusElem);
        lenient().when(statusElem.count()).thenReturn(1);
        lenient().when(statusElem.textContent()).thenReturn("경기종료");

        // Away Team
        setupTeamLocator(gameElement, ".team.away", "3");

        // Home Team
        setupTeamLocator(gameElement, ".team.home", "5");

        return gameElement;
    }

    private void setupTeamLocator(Locator gameElement, String teamSelector, String score) {
        Locator teamLocator = mock(Locator.class);
        Locator scoreLocator = mock(Locator.class);
        Locator pitcherLocator = mock(Locator.class);

        lenient().when(gameElement.locator(teamSelector)).thenReturn(teamLocator);
        lenient().when(teamLocator.count()).thenReturn(1);

        lenient().when(teamLocator.locator(".score")).thenReturn(scoreLocator);
        lenient().when(scoreLocator.count()).thenReturn(1);
        lenient().when(scoreLocator.textContent()).thenReturn(score);
        lenient().when(scoreLocator.getAttribute("class")).thenReturn("score");

        lenient().when(teamLocator.locator(".today-pitcher p")).thenReturn(pitcherLocator);
        lenient().when(pitcherLocator.count()).thenReturn(0);
    }
}

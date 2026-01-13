package yagubogu.crawling.game.service.crawler.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.KboScoreboardGame;


@ExtendWith(MockitoExtension.class)
class KboScoreboardPageTest {

    @Mock
    private Page mockPage;

    @Mock
    private KboCrawlerProperties mockProperties;

    @Mock
    private KboCrawlerProperties.CrawlerConfig mockCrawlerConfig;

    @Mock
    private KboCrawlerProperties.Selectors mockSelectors;

    @Mock
    private KboCrawlerProperties.ScoreboardSelectors mockScoreboardSelectors;

    @Mock
    private KboCrawlerProperties.TeamSelectors mockHomeTeamSelectors;

    @Mock
    private KboCrawlerProperties.TeamSelectors mockAwayTeamSelectors;

    @Mock
    private KboCrawlerProperties.ScoreTableSelectors mockScoreTableSelectors;

    @Mock
    private KboCrawlerProperties.PitcherSelectors mockPitcherSelectors;

    @Mock
    private KboCrawlerProperties.CalendarSelectors mockCalendarSelectors;

    @Mock
    private KboCrawlerProperties.Patterns mockPatterns;

    private KboScoreboardPage scoreboardPage;

    @BeforeEach
    void setUp() {
        // Properties 연결
        lenient().when(mockProperties.getCrawler()).thenReturn(mockCrawlerConfig);
        lenient().when(mockProperties.getSelectors()).thenReturn(mockSelectors);
        lenient().when(mockProperties.getPatterns()).thenReturn(mockPatterns);

        // CrawlerConfig 설정
        lenient().when(mockCrawlerConfig.getBaseUrl())
                .thenReturn("https://www.koreabaseball.com");
        lenient().when(mockCrawlerConfig.getScoreBoardUrl())
                .thenReturn("https://www.koreabaseball.com/scoreboard");
        lenient().when(mockCrawlerConfig.getNavigationTimeout())
                .thenReturn(Duration.ofSeconds(30));
        lenient().when(mockCrawlerConfig.getWaitTimeout())
                .thenReturn(Duration.ofSeconds(5));

        // Selectors 설정
        lenient().when(mockSelectors.getScoreboard()).thenReturn(mockScoreboardSelectors);
        lenient().when(mockSelectors.getCalendar()).thenReturn(mockCalendarSelectors);

        // ScoreboardSelectors 설정
        lenient().when(mockScoreboardSelectors.getContainer()).thenReturn(".scoreboard-container");
        lenient().when(mockScoreboardSelectors.getStatus()).thenReturn(".status");
        lenient().when(mockScoreboardSelectors.getStadium()).thenReturn(".stadium");
        lenient().when(mockScoreboardSelectors.getStartTime()).thenReturn(".start-time");
        lenient().when(mockScoreboardSelectors.getBoxScoreLink()).thenReturn("a.box-score");
        lenient().when(mockScoreboardSelectors.getHomeTeam()).thenReturn(mockHomeTeamSelectors);
        lenient().when(mockScoreboardSelectors.getAwayTeam()).thenReturn(mockAwayTeamSelectors);
        lenient().when(mockScoreboardSelectors.getScoreTable()).thenReturn(mockScoreTableSelectors);
        lenient().when(mockScoreboardSelectors.getPitcher()).thenReturn(mockPitcherSelectors);

        // TeamSelectors 설정
        lenient().when(mockHomeTeamSelectors.getName()).thenReturn(".home-team .name");
        lenient().when(mockHomeTeamSelectors.getScore()).thenReturn(".home-team .score");
        lenient().when(mockAwayTeamSelectors.getName()).thenReturn(".away-team .name");
        lenient().when(mockAwayTeamSelectors.getScore()).thenReturn(".away-team .score");

        // ScoreTableSelectors 설정
        lenient().when(mockScoreTableSelectors.getTable()).thenReturn("table.score-table");
        lenient().when(mockScoreTableSelectors.getRows()).thenReturn("tr");
        lenient().when(mockScoreTableSelectors.getTeamName()).thenReturn("td.team-name");
        lenient().when(mockScoreTableSelectors.getCells()).thenReturn("td");

        // PitcherSelectors 설정
        lenient().when(mockPitcherSelectors.getContainer()).thenReturn(".pitcher-info");
        lenient().when(mockPitcherSelectors.getSpans()).thenReturn("span");

        // CalendarSelectors 설정
        lenient().when(mockCalendarSelectors.getUpdatePanel()).thenReturn("#update-panel");

        // Patterns 설정
        lenient().when(mockPatterns.getTimeFormat()).thenReturn("HH:mm");
        lenient().when(mockPatterns.getPitcherLabel()).thenReturn("(승|패|세)\\s+(.+)");

        scoreboardPage = new KboScoreboardPage(mockPage, mockProperties);
    }

    @Nested
    @DisplayName("스코어보드 존재 확인 테스트")
    class ScoreboardExistenceTests {

        @Test
        @DisplayName("hasScoreboards - 스코어보드 존재")
        void hasScoreboards_Exists() {
            // Given
            // Mock은 기본적으로 예외를 던지지 않음

            // When
            boolean result = scoreboardPage.hasScoreboards();

            // Then
            assertThat(result).isTrue();
            verify(mockPage).waitForSelector(
                    eq(".scoreboard-container"),
                    any(Page.WaitForSelectorOptions.class)
            );
        }

        @Test
        @DisplayName("hasScoreboards - 스코어보드 없음 (Timeout)")
        void hasScoreboards_NotExists() {
            // Given
            doThrow(new TimeoutError("Timeout")).when(mockPage).waitForSelector(
                    eq(".scoreboard-container"),
                    any(Page.WaitForSelectorOptions.class)
            );

            // When
            boolean result = scoreboardPage.hasScoreboards();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("hasScoreboards - PlaywrightException 발생")
        void hasScoreboards_PlaywrightException() {
            // Given
            doThrow(new PlaywrightException("Network error")).when(mockPage).waitForSelector(
                    eq(".scoreboard-container"),
                    any(Page.WaitForSelectorOptions.class)
            );

            // When
            boolean result = scoreboardPage.hasScoreboards();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("스코어보드 목록 조회 테스트")
    class GetScoreboardsTests {

        @Test
        @DisplayName("getScoreboards - 여러 개 스코어보드 반환")
        void getScoreboards_MultipleScoreboards() {
            // Given
            ElementHandle scoreboard1 = mock(ElementHandle.class);
            ElementHandle scoreboard2 = mock(ElementHandle.class);
            ElementHandle scoreboard3 = mock(ElementHandle.class);

            when(mockPage.querySelectorAll(".scoreboard-container"))
                    .thenReturn(List.of(scoreboard1, scoreboard2, scoreboard3));

            // When
            List<ElementHandle> result = scoreboardPage.getScoreboards();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(scoreboard1, scoreboard2, scoreboard3);
        }

        @Test
        @DisplayName("getScoreboards - 빈 목록 반환")
        void getScoreboards_EmptyList() {
            // Given
            when(mockPage.querySelectorAll(".scoreboard-container"))
                    .thenReturn(List.of());

            // When
            List<ElementHandle> result = scoreboardPage.getScoreboards();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("스코어보드 파싱 테스트")
    class ParseScoreboardTests {

        @Test
        @DisplayName("parseScoreboard - 완전한 데이터 파싱 성공")
        void parseScoreboard_CompleteData() {
            // Given
            ElementHandle mockScoreboard = createCompleteScoreboardElement();
            LocalDate date = LocalDate.of(2025, 10, 26);

            // When
            Optional<KboScoreboardGame> result = scoreboardPage.parseScoreboard(mockScoreboard, date);

            // Then
            assertThat(result).isPresent();
            KboScoreboardGame game = result.get();

            assertThat(game.getDate()).isEqualTo(date);
            assertThat(game.getStatus()).isEqualTo("경기종료");
            assertThat(game.getStadium()).isEqualTo("잠실");
            assertThat(game.getStartTime()).isEqualTo(LocalTime.of(18, 30));
            assertThat(game.getHomeScore()).isEqualTo(5);
            assertThat(game.getAwayScore()).isEqualTo(3);
        }

        @Test
        @DisplayName("parseScoreboard - 팀 정보 없으면 빈 Optional")
        void parseScoreboard_NoTeamInfo_ReturnsEmpty() {
            // Given
            ElementHandle mockScoreboard = mock(ElementHandle.class);
            when(mockScoreboard.querySelector(anyString())).thenReturn(null);
            LocalDate date = LocalDate.of(2025, 10, 26);

            // When
            Optional<KboScoreboardGame> result = scoreboardPage.parseScoreboard(mockScoreboard, date);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("parseScoreboard - 투수 정보 파싱")
        void parseScoreboard_WithPitchers() {
            // Given
            ElementHandle mockScoreboard = createScoreboardWithPitchers();
            LocalDate date = LocalDate.of(2025, 10, 26);

            // When
            Optional<KboScoreboardGame> result = scoreboardPage.parseScoreboard(mockScoreboard, date);

            // Then
            assertThat(result).isPresent();
            KboScoreboardGame game = result.get();

            assertThat(game.getWinningPitcher()).isEqualTo("김광현");
            assertThat(game.getLosingPitcher()).isEqualTo("엄상백");
            assertThat(game.getSavingPitcher()).isEqualTo("고우석");
        }

        @Test
        @DisplayName("parseScoreboard - BoxScore URL 파싱")
        void parseScoreboard_WithBoxScoreUrl() {
            // Given
            ElementHandle mockScoreboard = createCompleteScoreboardElement();
            ElementHandle mockAnchor = mock(ElementHandle.class);

            when(mockScoreboard.querySelector("a.box-score")).thenReturn(mockAnchor);
            when(mockAnchor.getAttribute("href")).thenReturn("/game/boxscore/20251026LGKT");

            LocalDate date = LocalDate.of(2025, 10, 26);

            // When
            Optional<KboScoreboardGame> result = scoreboardPage.parseScoreboard(mockScoreboard, date);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getBoxScoreUrl())
                    .isEqualTo("https://www.koreabaseball.com/game/boxscore/20251026LGKT");
        }
    }

    @Nested
    @DisplayName("페이지 설정 테스트")
    class PageConfigTests {

        @Test
        @DisplayName("getBaseUrl - 스코어보드 URL 반환")
        void getBaseUrl_ReturnsScoreboardUrl() {
            // When
            String url = scoreboardPage.getBaseUrl();

            // Then
            assertThat(url).isEqualTo("https://www.koreabaseball.com/scoreboard");
        }

        @Test
        @DisplayName("needsDateChangeValidation - true 반환")
        void needsDateChangeValidation_ReturnsTrue() {
            // When
            boolean result = scoreboardPage.needsDateChangeValidation();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("waitForContentUpdate - 업데이트 패널 대기")
        void waitForContentUpdate_WaitsForUpdatePanel() {
            // Given
            long timeout = 5000L;
            Locator mockLocator = mock(Locator.class);

            when(mockPage.locator("#update-panel")).thenReturn(mockLocator);

            // When
            scoreboardPage.waitForContentUpdate(timeout);

            // Then
            verify(mockPage).locator("#update-panel");
            verify(mockLocator).waitFor(any(Locator.WaitForOptions.class));
        }
    }

    // ==================== 헬퍼 메서드 ====================

    private ElementHandle createCompleteScoreboardElement() {
        ElementHandle mockScoreboard = mock(ElementHandle.class);

        // 기본 정보
        ElementHandle statusElem = createMockElementWithText("경기종료");
        ElementHandle stadiumElem = createMockElementWithText("잠실 18:30");
        ElementHandle startTimeElem = createMockElementWithText("18:30");

        lenient().when(mockScoreboard.querySelector(".status")).thenReturn(statusElem);
        lenient().when(mockScoreboard.querySelector(".stadium")).thenReturn(stadiumElem);
        lenient().when(mockScoreboard.querySelector(".start-time")).thenReturn(startTimeElem);

        // Away 팀
        ElementHandle awayNameElem = createMockElementWithText("KT");
        ElementHandle awayScoreElem = createMockElementWithText("3");

        lenient().when(mockScoreboard.querySelector(".away-team .name")).thenReturn(awayNameElem);
        lenient().when(mockScoreboard.querySelector(".away-team .score")).thenReturn(awayScoreElem);

        // Home 팀
        ElementHandle homeNameElem = createMockElementWithText("LG");
        ElementHandle homeScoreElem = createMockElementWithText("5");

        lenient().when(mockScoreboard.querySelector(".home-team .name")).thenReturn(homeNameElem);
        lenient().when(mockScoreboard.querySelector(".home-team .score")).thenReturn(homeScoreElem);

        // BoxScore Link
        ElementHandle boxScoreLink = mock(ElementHandle.class);
        lenient().when(boxScoreLink.getAttribute("href")).thenReturn("/game/boxscore/20251026LGKT");
        lenient().when(mockScoreboard.querySelector("a.box-score")).thenReturn(boxScoreLink);

        // 스코어 테이블
        ElementHandle mockTable = createMockScoreTable();
        lenient().when(mockScoreboard.querySelector("table.score-table")).thenReturn(mockTable);

        // Pitcher 정보 (기본값: 없음)
        ElementHandle pitcherContainer = mock(ElementHandle.class);
        lenient().when(mockScoreboard.querySelector(".pitcher-info")).thenReturn(pitcherContainer);
        lenient().when(pitcherContainer.querySelectorAll("span")).thenReturn(List.of());

        return mockScoreboard;
    }

    private ElementHandle createScoreboardWithPitchers() {
        ElementHandle mockScoreboard = createCompleteScoreboardElement();

        // 투수 정보 추가
        ElementHandle pitcherContainer = mock(ElementHandle.class);
        lenient().when(mockScoreboard.querySelector(".pitcher-info")).thenReturn(pitcherContainer);

        ElementHandle winPitcher = createMockElementWithText("승 김광현");
        ElementHandle losePitcher = createMockElementWithText("패 엄상백");
        ElementHandle savePitcher = createMockElementWithText("세 고우석");

        lenient().when(pitcherContainer.querySelectorAll("span"))
                .thenReturn(List.of(winPitcher, losePitcher, savePitcher));

        return mockScoreboard;
    }

    private ElementHandle createMockScoreTable() {
        ElementHandle mockTable = mock(ElementHandle.class);

        // LG 팀 행
        ElementHandle lgRow = mock(ElementHandle.class);
        ElementHandle lgTeamName = createMockElementWithText("LG");
        ElementHandle lgScore1 = createMockElementWithText("0");
        ElementHandle lgScore2 = createMockElementWithText("2");
        ElementHandle lgScore3 = createMockElementWithText("0");
        ElementHandle lgRuns = createMockElementWithText("5");
        ElementHandle lgHits = createMockElementWithText("10");
        ElementHandle lgErrors = createMockElementWithText("1");
        ElementHandle lgBalls = createMockElementWithText("3");

        lenient().when(lgRow.querySelector("td.team-name")).thenReturn(lgTeamName);
        lenient().when(lgRow.querySelectorAll("td")).thenReturn(
                List.of(lgTeamName, lgScore1, lgScore2, lgScore3, lgRuns, lgHits, lgErrors, lgBalls)
        );

        // KT 팀 행
        ElementHandle ktRow = mock(ElementHandle.class);
        ElementHandle ktTeamName = createMockElementWithText("KT");
        ElementHandle ktScore1 = createMockElementWithText("1");
        ElementHandle ktScore2 = createMockElementWithText("0");
        ElementHandle ktScore3 = createMockElementWithText("2");
        ElementHandle ktRuns = createMockElementWithText("3");
        ElementHandle ktHits = createMockElementWithText("8");
        ElementHandle ktErrors = createMockElementWithText("0");
        ElementHandle ktBalls = createMockElementWithText("2");

        lenient().when(ktRow.querySelector("td.team-name")).thenReturn(ktTeamName);
        lenient().when(ktRow.querySelectorAll("td")).thenReturn(
                List.of(ktTeamName, ktScore1, ktScore2, ktScore3, ktRuns, ktHits, ktErrors, ktBalls)
        );

        lenient().when(mockTable.querySelectorAll("tr")).thenReturn(List.of(lgRow, ktRow));

        return mockTable;
    }

    private ElementHandle createMockElementWithText(String text) {
        ElementHandle elem = mock(ElementHandle.class);
        lenient().when(elem.innerText()).thenReturn(text);
        return elem;
    }
}

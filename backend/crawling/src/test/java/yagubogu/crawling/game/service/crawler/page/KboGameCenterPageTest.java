package yagubogu.crawling.game.service.crawler.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.GameCenterDetail;

@ExtendWith(MockitoExtension.class)
class KboGameCenterPageTest {

    @Mock
    private Page mockPage;

    @Mock
    private KboCrawlerProperties mockProperties;

    @Mock
    private KboCrawlerProperties.CrawlerConfig mockCrawlerConfig;

    @Mock
    private KboCrawlerProperties.Selectors mockSelectors;

    @Mock
    private KboCrawlerProperties.Patterns mockPatterns;

    private KboGameCenterPage gameCenterPage;

    @BeforeEach
    void setUp() {
        // Properties 연결
        lenient().when(mockProperties.getCrawler()).thenReturn(mockCrawlerConfig);
        lenient().when(mockProperties.getSelectors()).thenReturn(mockSelectors);
        lenient().when(mockProperties.getPatterns()).thenReturn(mockPatterns);

        // CrawlerConfig 설정
        lenient().when(mockCrawlerConfig.getGameCenterUrl())
                .thenReturn("https://www.koreabaseball.com/gameCenter");
        lenient().when(mockCrawlerConfig.getNavigationTimeout())
                .thenReturn(Duration.ofSeconds(30));
        lenient().when(mockCrawlerConfig.getWaitTimeout())
                .thenReturn(Duration.ofSeconds(5));

        gameCenterPage = new KboGameCenterPage(mockPage, mockProperties);
    }

    @Nested
    @DisplayName("데이터 추출 테스트")
    class DataExtractionTests {

        @Test
        @DisplayName("getDateText - 날짜 텍스트 정상 추출")
        void getDateText_Success() {
            // Given
            Locator mockLocator = mock(Locator.class);
            when(mockPage.locator("#lblGameDate")).thenReturn(mockLocator);
            when(mockLocator.textContent()).thenReturn("2025.10.26 (일)");

            // When
            String result = gameCenterPage.getDateText();

            // Then
            assertThat(result).isEqualTo("2025-10-26");
        }

        @Test
        @DisplayName("getGameCount - 경기 개수 정상 반환")
        void getGameCount_Success() {
            // Given
            Locator mockLocator = mock(Locator.class);
            when(mockPage.locator(".game-list-n > li.game-cont")).thenReturn(mockLocator);
            when(mockLocator.count()).thenReturn(5);

            // When
            int count = gameCenterPage.getGameCount();

            // Then
            assertThat(count).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("경기 상세 정보 추출 테스트")
    class ExtractGameDetailTests {

        @Test
        @DisplayName("extractGameDetail - class에 ing가 있으면 gameStatus는 경기중")
        void extractGameDetail_InProgress_SetsGameStatusInProgress() {
            // Given
            Locator gameElement = createMinimalGameElement("game-cont ing");

            // When
            GameCenterDetail result = gameCenterPage.extractGameDetail(gameElement, "20260621");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGameStatus()).isEqualTo("경기중");
        }

        @Test
        @DisplayName("extractGameDetail - class에 end가 있으면 gameStatus는 경기종료")
        void extractGameDetail_Ended_SetsGameStatusEnded() {
            // Given
            Locator gameElement = createMinimalGameElement("game-cont end");

            // When
            GameCenterDetail result = gameCenterPage.extractGameDetail(gameElement, "20260621");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGameStatus()).isEqualTo("경기종료");
        }

        @Test
        @DisplayName("extractGameDetail - end/cancel/ing 모두 없으면 gameStatus는 경기예정")
        void extractGameDetail_Scheduled_SetsGameStatusScheduled() {
            // Given
            Locator gameElement = createMinimalGameElement("game-cont");

            // When
            GameCenterDetail result = gameCenterPage.extractGameDetail(gameElement, "20260621");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGameStatus()).isEqualTo("경기예정");
        }

        private Locator createMinimalGameElement(String classAttr) {
            Locator gameElement = mock(Locator.class);
            lenient().when(gameElement.getAttribute("class")).thenReturn(classAttr);

            lenient().when(gameElement.locator(".top > ul > li")).thenReturn(mock(Locator.class));
            lenient().when(gameElement.locator(".middle .broadcasting")).thenReturn(mock(Locator.class));
            lenient().when(gameElement.locator(".middle .staus")).thenReturn(mock(Locator.class));
            lenient().when(gameElement.locator(".team.away")).thenReturn(mock(Locator.class));
            lenient().when(gameElement.locator(".team.home")).thenReturn(mock(Locator.class));

            return gameElement;
        }
    }

    @Nested
    @DisplayName("페이지 설정 테스트")
    class PageConfigTests {

        @Test
        @DisplayName("getBaseUrl - 게임센터 URL 반환")
        void getBaseUrl_ReturnsGameCenterUrl() {
            // When
            String url = gameCenterPage.getBaseUrl();

            // Then
            assertThat(url).isEqualTo("https://www.koreabaseball.com/gameCenter");
        }

        @Test
        @DisplayName("needsDateChangeValidation - false 반환")
        void needsDateChangeValidation_ReturnsFalse() {
            // When
            boolean result = gameCenterPage.needsDateChangeValidation();

            // Then
            assertThat(result).isFalse();
        }
    }
}

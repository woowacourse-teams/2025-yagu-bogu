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

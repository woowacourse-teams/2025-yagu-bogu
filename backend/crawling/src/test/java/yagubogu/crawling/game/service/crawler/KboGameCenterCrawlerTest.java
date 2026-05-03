package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;

@ExtendWith(MockitoExtension.class)
class KboGameCenterCrawlerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private KboHttpClient kboHttpClient;

    private KboGameCenterCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new KboGameCenterCrawler(kboHttpClient);
    }

    @Nested
    @DisplayName("일일 크롤링 테스트")
    class DailyCrawlingTests {

        @Test
        @DisplayName("fetchDailyGameCenter - 정상 크롤링 성공")
        void fetchDailyGameCenter_Success() throws Exception {
            // Given
            LocalDate date = LocalDate.of(2026, 4, 12);
            when(kboHttpClient.fetchGameList(date)).thenReturn(OBJECT_MAPPER.readTree("""
                    {
                      "game": [
                        {
                          "G_ID": "20260412SKLG0",
                          "G_DT": "20260412",
                          "GAME_STATE_SC": "3",
                          "GAME_INN_NO": 9,
                          "GAME_TB_SC_NM": "초",
                          "AWAY_ID": "SK",
                          "HOME_ID": "LG",
                          "AWAY_NM": "SSG",
                          "HOME_NM": "LG",
                          "S_NM": "잠실",
                          "G_TM": "14:00",
                          "TV_IF": "SPO-T",
                          "T_SCORE_CN": "1",
                          "B_SCORE_CN": "9",
                          "T_PIT_P_NM": "베니지아노",
                          "B_PIT_P_NM": "톨허스트"
                        }
                      ],
                      "code": "100"
                    }
                    """));

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result.getDate()).isEqualTo("2026-04-12");
            assertThat(result.getGames()).hasSize(1);

            GameCenterDetail game = result.getGames().getFirst();
            assertThat(game.getGameCode()).isEqualTo("20260412SKLG0");
            assertThat(game.getGameDate()).isEqualTo("20260412");
            assertThat(game.getStatus()).isEqualTo("경기종료");
            assertThat(game.getStadiumName()).isEqualTo("잠실");
            assertThat(game.getWinner()).isEqualTo("home");
            assertThat(game.getAwayPitchers()).containsExactly("선발 : 베니지아노");
            assertThat(game.getHomePitchers()).containsExactly("선발 : 톨허스트");
        }

        @Test
        @DisplayName("fetchDailyGameCenter - 경기 없음")
        void fetchDailyGameCenter_NoGames() throws Exception {
            // Given
            LocalDate date = LocalDate.of(2026, 4, 13);
            when(kboHttpClient.fetchGameList(date)).thenReturn(OBJECT_MAPPER.readTree("""
                    {
                      "game": [],
                      "code": "100"
                    }
                    """));

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo("2026-04-13");
            assertThat(result.getGames()).isEmpty();
        }

        @Test
        @DisplayName("fetchDailyGameCenter - 예외 발생 시 빈 결과")
        void fetchDailyGameCenter_ExceptionReturnsEmpty() {
            // Given
            LocalDate date = LocalDate.of(2026, 4, 12);
            when(kboHttpClient.fetchGameList(date)).thenThrow(new RuntimeException("KBO server error"));

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo("2026-04-12");
            assertThat(result.getGames()).isEmpty();
        }
    }
}

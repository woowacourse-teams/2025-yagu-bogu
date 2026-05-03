package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.jsoup.Jsoup;
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

    @Mock
    private KboHtmlClient kboHtmlClient;

    private KboGameCenterCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new KboGameCenterCrawler(kboHtmlClient);
    }

    @Nested
    @DisplayName("일일 크롤링 테스트")
    class DailyCrawlingTests {

        @Test
        @DisplayName("fetchDailyGameCenter - 서버 렌더링 HTML 크롤링 성공")
        void fetchDailyGameCenter_Success() {
            // Given
            LocalDate date = LocalDate.of(2026, 4, 12);
            when(kboHtmlClient.fetchGameCenter(date)).thenReturn(Jsoup.parse("""
                    <ul class="game-list-n">
                      <li class="game-cont end" g_id="20260412SKLG0" g_dt="20260412" game_sc="3"
                          away_id="SK" home_id="LG" away_nm="SSG" home_nm="LG" s_nm="잠실">
                        <div class="top"><ul><li>잠실</li><li></li><li>14:00</li></ul></div>
                        <div class="middle"><span class="broadcasting">SPO-T</span><span class="staus">경기종료</span></div>
                        <div class="team away"><em class="score">1</em><div class="today-pitcher"><p>베니지아노</p></div></div>
                        <div class="team home"><em class="score win">9</em><div class="today-pitcher"><p>톨허스트</p></div></div>
                      </li>
                    </ul>
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
            assertThat(game.getAwayPitchers()).containsExactly("베니지아노");
            assertThat(game.getHomePitchers()).containsExactly("톨허스트");
        }

        @Test
        @DisplayName("fetchDailyGameCenter - HTML에 경기 목록 없음")
        void fetchDailyGameCenter_NoGames() {
            // Given
            LocalDate date = LocalDate.of(2026, 4, 13);
            when(kboHtmlClient.fetchGameCenter(date)).thenReturn(Jsoup.parse("<div class=\"today-game\"></div>"));

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
            when(kboHtmlClient.fetchGameCenter(date)).thenThrow(new RuntimeException("KBO server error"));

            // When
            GameCenter result = crawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo("2026-04-12");
            assertThat(result.getGames()).isEmpty();
        }
    }
}

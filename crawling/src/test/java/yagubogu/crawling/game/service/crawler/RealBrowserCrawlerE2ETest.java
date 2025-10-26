package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RealBrowserCrawlerE2ETest {

    @Autowired
    private KboGameCenterCrawler gameCenterCrawler;

    @Autowired
    private KboScoreboardCrawler scoreboardCrawler;

    @Nested
    @DisplayName("실제 KBO 게임센터 E2E 테스트")
    class GameCenterE2ETests {

        @Test
        @DisplayName("실제 게임센터 크롤링 - 과거 확정 날짜")
        @Disabled("실제 브라우저 구동 필요 - CI/CD에서는 제외")
        void realGameCenterCrawling_PastDate() {
            // Given
            LocalDate date = LocalDate.of(2024, 10, 1);

            // When
            GameCenter result = gameCenterCrawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo("2024-10-01");

            System.out.println("=== 게임센터 크롤링 결과 ===");
            System.out.println("날짜: " + result.getDate());
            System.out.println("경기 수: " + result.getGames().size());

            if (!result.getGames().isEmpty()) {
                System.out.println("\n첫 번째 경기:");
                GameCenterDetail firstGame = result.getGames().get(0);
                System.out.println("  - 홈팀: " + firstGame.getHomeTeamName());
                System.out.println("  - 원정팀: " + firstGame.getAwayTeamName());
                System.out.println("  - 경기장: " + firstGame.getStadiumName());
                System.out.println("  - 상태: " + firstGame.getGameStatus());
            }
        }

        @Test
        @DisplayName("실제 게임센터 크롤링 - 경기 없는 날")
        @Disabled("실제 브라우저 구동 필요")
        void realGameCenterCrawling_NoGameDay() {
            // Given
            LocalDate date = LocalDate.of(2024, 12, 25); // 크리스마스 (경기 없음)

            // When
            GameCenter result = gameCenterCrawler.fetchDailyGameCenter(date);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGames()).isEmpty();
            System.out.println("경기 없는 날 확인: " + result.getDate());
        }
    }

    @Nested
    @DisplayName("실제 KBO 스코어보드 E2E 테스트")
    class ScoreboardE2ETests {

        @Test
        @DisplayName("실제 스코어보드 크롤링 - 단일 날짜")
        @Disabled("실제 브라우저 구동 필요")
        void realScoreboardCrawling_SingleDate() {
            // Given
            List<LocalDate> dates = List.of(LocalDate.of(2024, 10, 1));

            // When
            Map<LocalDate, List<KboScoreboardGame>> result = scoreboardCrawler.crawl(dates);

            // Then
            assertThat(result).isNotEmpty();

            System.out.println("=== 스코어보드 크롤링 결과 ===");
            result.forEach((date, games) -> {
                System.out.println("\n날짜: " + date);
                System.out.println("경기 수: " + games.size());

                games.forEach(game -> {
                    System.out.println("  - " + game.getAwayTeamScoreboard().name() +
                            " vs " + game.getHomeTeamScoreboard().name());
                    System.out.println("    점수: " + game.getAwayScore() + " : " + game.getHomeScore());
                    System.out.println("    승: " + game.getWinningPitcher());
                    System.out.println("    패: " + game.getLosingPitcher());
                });
            });
        }

        @Test
        @DisplayName("실제 스코어보드 크롤링 - 여러 날짜")
        @Disabled("실제 브라우저 구동 필요")
        void realScoreboardCrawling_MultipleDates() {
            // Given
            List<LocalDate> dates = List.of(
                    LocalDate.of(2024, 10, 1),
                    LocalDate.of(2024, 10, 2),
                    LocalDate.of(2024, 10, 3)
            );

            // When
            Map<LocalDate, List<KboScoreboardGame>> result = scoreboardCrawler.crawl(dates);

            // Then
            assertThat(result).hasSize(3);

            int totalGames = result.values().stream()
                    .mapToInt(List::size)
                    .sum();

            System.out.println("=== 다중 날짜 크롤링 결과 ===");
            System.out.println("총 날짜 수: " + result.size());
            System.out.println("총 경기 수: " + totalGames);
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTests {

        @Test
        @DisplayName("게임센터 크롤링 성능 측정")
        @Disabled("실제 브라우저 구동 필요")
        void measureGameCenterPerformance() {
            // Given
            LocalDate date = LocalDate.of(2024, 10, 1);

            // When
            long startTime = System.currentTimeMillis();
            GameCenter result = gameCenterCrawler.fetchDailyGameCenter(date);
            long endTime = System.currentTimeMillis();

            // Then
            long duration = endTime - startTime;
            System.out.println("=== 성능 측정 ===");
            System.out.println("크롤링 시간: " + duration + "ms");
            System.out.println("경기 수: " + result.getGames().size());
            System.out.println("경기당 평균: " + (duration / Math.max(1, result.getGames().size())) + "ms");

            // 10초 이내에 완료되어야 함
            assertThat(duration).isLessThan(10000);
        }

        @Test
        @DisplayName("스코어보드 크롤링 성능 측정")
        @Disabled("실제 브라우저 구동 필요")
        void measureScoreboardPerformance() {
            // Given
            List<LocalDate> dates = List.of(
                    LocalDate.of(2024, 10, 1),
                    LocalDate.of(2024, 10, 2),
                    LocalDate.of(2024, 10, 3)
            );

            // When
            long startTime = System.currentTimeMillis();
            Map<LocalDate, List<KboScoreboardGame>> result = scoreboardCrawler.crawl(dates);
            long endTime = System.currentTimeMillis();

            // Then
            long duration = endTime - startTime;
            int totalGames = result.values().stream().mapToInt(List::size).sum();

            System.out.println("=== 성능 측정 ===");
            System.out.println("크롤링 시간: " + duration + "ms");
            System.out.println("총 경기 수: " + totalGames);
            System.out.println("날짜당 평균: " + (duration / dates.size()) + "ms");

            // 30초 이내에 완료되어야 함
            assertThat(duration).isLessThan(30000);
        }
    }

    @Nested
    @DisplayName("에러 처리 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("유효하지 않은 날짜 처리")
        @Disabled("실제 브라우저 구동 필요")
        void handleInvalidDate() {
            // Given
            LocalDate futureDate = LocalDate.now().plusYears(10);

            // When
            GameCenter result = gameCenterCrawler.fetchDailyGameCenter(futureDate);

            // Then
            assertThat(result).isNotNull();
            // 미래 날짜는 경기가 없어야 함
            assertThat(result.getGames()).isEmpty();
        }

        @Test
        @DisplayName("네트워크 타임아웃 처리")
        @Disabled("수동 테스트 - 네트워크 차단 필요")
        void handleNetworkTimeout() {
            // 이 테스트는 네트워크를 수동으로 차단한 상태에서 실행
            // 예상: 재시도 후 최종 실패
            LocalDate date = LocalDate.of(2024, 10, 1);

            assertThatThrownBy(() -> gameCenterCrawler.fetchDailyGameCenter(date))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}

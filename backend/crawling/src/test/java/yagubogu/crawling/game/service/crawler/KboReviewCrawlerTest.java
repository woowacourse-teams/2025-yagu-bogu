package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.KboReviewCrawler;

@ExtendWith(MockitoExtension.class)
class KboReviewCrawlerTest {

    private static final String GAME_CODE = "20260412SKLG0";

    @Mock
    private KboHtmlClient kboHtmlClient;

    private KboReviewCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new KboReviewCrawler(kboHtmlClient);
    }

    @Test
    @DisplayName("crawlReview - 서버 렌더링 HTML 테이블을 기록 DTO로 변환")
    void crawlReview_ConvertsHtmlTables() {
        // Given
        when(kboHtmlClient.fetchReview(GAME_CODE)).thenReturn(Jsoup.parse("""
                <table id="tblAwayHitter1"><tbody><tr><th>1</th><th>유</th><td>박성한</td></tr></tbody></table>
                <table id="tblAwayHitter3"><tbody><tr><td>4</td><td>1</td><td>0</td><td>1</td><td>0.250</td></tr></tbody></table>
                <table id="tblHomeHitter1"><tbody><tr><th>1</th><th>중</th><td>박해민</td></tr></tbody></table>
                <table id="tblHomeHitter3"><tbody><tr><td>5</td><td>2</td><td>1</td><td>2</td><td>0.400</td></tr></tbody></table>
                <table id="tblAwayPitcher"><tbody>
                  <tr><td>베니지아노</td><td></td><td>패</td><td>0</td><td>1</td><td>0</td><td>3</td><td>18</td><td>77</td><td>16</td><td>6</td><td>0</td><td>2</td><td>4</td><td>5</td><td>3</td><td>9.00</td></tr>
                </tbody></table>
                <table id="tblHomePitcher"><tbody>
                  <tr><td>톨허스트</td><td></td><td>승</td><td>1</td><td>0</td><td>0</td><td>6</td><td>23</td><td>91</td><td>21</td><td>4</td><td>0</td><td>1</td><td>7</td><td>1</td><td>1</td><td>1.50</td></tr>
                </tbody></table>
                """));

        // When
        ReviewData result = crawler.crawlReview(GAME_CODE);

        // Then
        assertThat(result.gameCode()).isEqualTo(GAME_CODE);
        assertThat(result.awayHitters()).containsExactly(
                new HitterRecordDto(1, "유", "박성한", 4, 1, 0, 1)
        );
        assertThat(result.homeHitters()).containsExactly(
                new HitterRecordDto(1, "중", "박해민", 5, 2, 1, 2)
        );
        assertThat(result.awayPitchers()).containsExactly(
                new PitcherRecordDto("베니지아노", "패", "3", 18, 77, 16, 6, 0, 2, 4, 5, 3)
        );
        assertThat(result.homePitchers()).containsExactly(
                new PitcherRecordDto("톨허스트", "승", "6", 23, 91, 21, 4, 0, 1, 7, 1, 1)
        );
    }
}

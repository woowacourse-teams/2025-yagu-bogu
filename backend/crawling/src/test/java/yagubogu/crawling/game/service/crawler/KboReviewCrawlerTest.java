package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.service.crawler.KboHttpClient.ReviewGameContext;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.KboReviewCrawler;

@ExtendWith(MockitoExtension.class)
class KboReviewCrawlerTest {

    private static final String GAME_CODE = "20260412SKLG0";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private KboHttpClient kboHttpClient;

    private KboReviewCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new KboReviewCrawler(kboHttpClient, objectMapper);
    }

    @Test
    @DisplayName("crawlReview - KBO box score JSON 테이블을 기록 DTO로 변환")
    void crawlReview_ConvertsBoxScoreTables() throws Exception {
        // Given
        ReviewGameContext context = new ReviewGameContext("1", "0", "2026", GAME_CODE);
        when(kboHttpClient.findReviewGameContext(GAME_CODE)).thenReturn(context);
        when(kboHttpClient.fetchBoxScore(context)).thenReturn(boxScoreJson());

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

    private JsonNode boxScoreJson() throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode hitters = root.putArray("arrHitter");
        hitters.add(hitterTable(new String[]{"1", "유", "박성한"}, new String[]{"4", "1", "0", "1", "0.250"}));
        hitters.add(hitterTable(new String[]{"1", "중", "박해민"}, new String[]{"5", "2", "1", "2", "0.400"}));

        ArrayNode pitchers = root.putArray("arrPitcher");
        pitchers.add(pitcherTable(new String[]{
                "베니지아노", "", "패", "0", "1", "0", "3", "18", "77", "16", "6", "0", "2", "4", "5", "3", "9.00"
        }));
        pitchers.add(pitcherTable(new String[]{
                "톨허스트", "", "승", "1", "0", "0", "6", "23", "91", "21", "4", "0", "1", "7", "1", "1", "1.50"
        }));

        return root;
    }

    private ObjectNode hitterTable(final String[] basicCells, final String[] statCells) throws Exception {
        ObjectNode table = objectMapper.createObjectNode();
        table.put("table1", tableJson(basicCells));
        table.put("table3", tableJson(statCells));
        return table;
    }

    private ObjectNode pitcherTable(final String[] cells) throws Exception {
        ObjectNode table = objectMapper.createObjectNode();
        table.put("table", tableJson(cells));
        return table;
    }

    private String tableJson(final String[] cells) throws Exception {
        ObjectNode table = objectMapper.createObjectNode();
        ArrayNode rows = table.putArray("rows");
        ObjectNode row = rows.addObject();
        ArrayNode rowCells = row.putArray("row");
        for (String cell : cells) {
            rowCells.addObject().put("Text", cell);
        }
        return objectMapper.writeValueAsString(table);
    }
}

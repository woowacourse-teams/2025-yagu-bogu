package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.service.crawler.KboHttpClient;
import yagubogu.crawling.game.service.crawler.KboHttpClient.ReviewGameContext;

@Slf4j
public class KboReviewCrawler {

    private final KboHttpClient kboHttpClient;
    private final ObjectMapper objectMapper;

    public KboReviewCrawler(final KboHttpClient kboHttpClient, final ObjectMapper objectMapper) {
        this.kboHttpClient = kboHttpClient;
        this.objectMapper = objectMapper;
    }

    public ReviewData crawlReview(final String gameCode) {
        log.info("[REVIEW] HTTP 크롤링 시작: gameCode={}", gameCode);

        ReviewGameContext context = kboHttpClient.findReviewGameContext(gameCode);
        JsonNode boxScore = kboHttpClient.fetchBoxScore(context);

        List<HitterRecordDto> awayHitters = extractHitterRecords(boxScore.path("arrHitter").path(0));
        List<HitterRecordDto> homeHitters = extractHitterRecords(boxScore.path("arrHitter").path(1));
        List<PitcherRecordDto> awayPitchers = extractPitcherRecords(boxScore.path("arrPitcher").path(0));
        List<PitcherRecordDto> homePitchers = extractPitcherRecords(boxScore.path("arrPitcher").path(1));

        log.info("[REVIEW] HTTP 크롤링 완료: gameCode={}", gameCode);
        return new ReviewData(gameCode, awayHitters, homeHitters, awayPitchers, homePitchers);
    }

    private List<HitterRecordDto> extractHitterRecords(final JsonNode hitterTables) {
        List<JsonNode> table1Rows = rowsOf(hitterTables.path("table1").asText());
        List<JsonNode> table3Rows = rowsOf(hitterTables.path("table3").asText());
        List<HitterRecordDto> records = new ArrayList<>();

        for (int i = 0; i < table1Rows.size(); i++) {
            if (i >= table3Rows.size()) {
                log.warn("타자 기록 table3 행 부족으로 건너뜀: i={}, table3RowCount={}", i, table3Rows.size());
                continue;
            }

            List<String> basicCells = cellTexts(table1Rows.get(i));
            List<String> statCells = cellTexts(table3Rows.get(i));
            if (basicCells.size() < 3 || statCells.size() < 4) {
                log.debug("타자 기록 행 구조 불일치, 건너뜀: basicCells={}, statCells={}", basicCells.size(), statCells.size());
                continue;
            }

            records.add(new HitterRecordDto(
                    parseIntSafe(basicCells.get(0)),
                    basicCells.get(1),
                    basicCells.get(2),
                    parseIntSafe(statCells.get(0)),
                    parseIntSafe(statCells.get(1)),
                    parseIntSafe(statCells.get(2)),
                    parseIntSafe(statCells.get(3))
            ));
        }

        return records;
    }

    private List<PitcherRecordDto> extractPitcherRecords(final JsonNode pitcherTable) {
        List<JsonNode> rows = rowsOf(pitcherTable.path("table").asText());
        List<PitcherRecordDto> records = new ArrayList<>();

        for (JsonNode row : rows) {
            List<String> cells = cellTexts(row);
            if (cells.size() < 16) {
                log.debug("투수 기록 행 구조 불일치, 건너뜀: cells={}", cells.size());
                continue;
            }

            records.add(new PitcherRecordDto(
                    cells.get(0),
                    emptyToNull(cells.get(2)),
                    cells.get(6),
                    parseIntSafe(cells.get(7)),
                    parseIntSafe(cells.get(8)),
                    parseIntSafe(cells.get(9)),
                    parseIntSafe(cells.get(10)),
                    parseIntSafe(cells.get(11)),
                    parseIntSafe(cells.get(12)),
                    parseIntSafe(cells.get(13)),
                    parseIntSafe(cells.get(14)),
                    parseIntSafe(cells.get(15))
            ));
        }

        return records;
    }

    private List<JsonNode> rowsOf(final String tableJson) {
        if (tableJson == null || tableJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode rows = objectMapper.readTree(tableJson).path("rows");
            if (!rows.isArray()) {
                return List.of();
            }

            List<JsonNode> result = new ArrayList<>();
            rows.forEach(result::add);
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("KBO 리뷰 테이블 JSON 파싱 실패", e);
        }
    }

    private List<String> cellTexts(final JsonNode row) {
        JsonNode cells = row.path("row");
        if (!cells.isArray()) {
            return List.of();
        }

        List<String> texts = new ArrayList<>();
        for (JsonNode cell : cells) {
            texts.add(cell.path("Text").asText("").trim());
        }
        return texts;
    }

    private int parseIntSafe(final String text) {
        Integer value = parseNullableInt(text);
        return value != null ? value : 0;
    }

    private Integer parseNullableInt(final String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.replaceAll("[^0-9-]", "").trim();
        if (normalized.isEmpty() || "-".equals(normalized)) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String emptyToNull(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

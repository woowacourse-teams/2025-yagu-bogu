package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.service.crawler.KboHtmlClient;

@Slf4j
public class KboReviewCrawler {

    private final KboHtmlClient kboHtmlClient;

    public KboReviewCrawler(final KboHtmlClient kboHtmlClient) {
        this.kboHtmlClient = kboHtmlClient;
    }

    public ReviewData crawlReview(final String gameCode) {
        log.info("[REVIEW] HTTP 크롤링 시작: gameCode={}", gameCode);

        Document document = kboHtmlClient.fetchReview(gameCode);

        List<HitterRecordDto> awayHitters = extractHitterRecords(document, "tblAwayHitter1", "tblAwayHitter3");
        List<HitterRecordDto> homeHitters = extractHitterRecords(document, "tblHomeHitter1", "tblHomeHitter3");
        List<PitcherRecordDto> awayPitchers = extractPitcherRecords(document, "tblAwayPitcher");
        List<PitcherRecordDto> homePitchers = extractPitcherRecords(document, "tblHomePitcher");

        log.info("[REVIEW] HTTP 크롤링 완료: gameCode={}", gameCode);
        return new ReviewData(gameCode, awayHitters, homeHitters, awayPitchers, homePitchers);
    }

    private List<HitterRecordDto> extractHitterRecords(final Document document, final String table1Id, final String table3Id) {
        Elements table1Rows = document.select("#" + table1Id + " tbody tr");
        Elements table3Rows = document.select("#" + table3Id + " tbody tr");
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

    private List<PitcherRecordDto> extractPitcherRecords(final Document document, final String tableId) {
        Elements rows = document.select("#" + tableId + " tbody tr");
        List<PitcherRecordDto> records = new ArrayList<>();

        for (Element row : rows) {
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

    private List<String> cellTexts(final Element row) {
        List<String> texts = new ArrayList<>();
        for (Element cell : row.select("th, td")) {
            texts.add(cell.text().trim());
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

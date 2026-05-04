package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import com.yagubogu.game.exception.GameSyncException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;

@Slf4j
public class KboScoreboardParser {

    private final KboCrawlerProperties properties;
    private final Pattern pitcherPattern;
    private final DateTimeFormatter timeFormatter;

    public KboScoreboardParser(final KboCrawlerProperties properties) {
        this.properties = properties;
        this.pitcherPattern = Pattern.compile(properties.getPatterns().getPitcherLabel());
        this.timeFormatter = DateTimeFormatter.ofPattern(properties.getPatterns().getTimeFormat());
    }

    public List<KboScoreboardGame> parse(final Document document, final LocalDate date) {
        String containerSelector = properties.getSelectors().getScoreboard().getContainer();
        Elements scoreboards = document.select(containerSelector);
        if (scoreboards.isEmpty()) {
            log.info("스코어보드가 존재하지 않습니다.");
            return List.of();
        }

        List<KboScoreboardGame> games = new ArrayList<>();
        for (Element scoreboard : scoreboards) {
            parseScoreboard(scoreboard, date).ifPresent(games::add);
        }
        return games;
    }

    Optional<KboScoreboardGame> parseScoreboard(final Element scoreboard, final LocalDate date) {
        var selectors = properties.getSelectors().getScoreboard();

        String status = safeText(scoreboard, selectors.getStatus());
        String stadium = safeText(scoreboard, selectors.getStadium());
        String startTime = safeText(scoreboard, selectors.getStartTime());

        log.debug("[DEBUG] Parsed - status: {}, stadium: {}, startTime: {}", status, stadium, startTime);

        stadium = cleanStadiumName(stadium, startTime);

        String awayName = safeText(scoreboard, selectors.getAwayTeam().getName());
        String homeName = safeText(scoreboard, selectors.getHomeTeam().getName());
        Integer awayScore = parseNullableInt(safeText(scoreboard, selectors.getAwayTeam().getScore()));
        Integer homeScore = parseNullableInt(safeText(scoreboard, selectors.getHomeTeam().getScore()));

        log.debug("[DEBUG] Parsed - awayName: {}, homeName: {}, awayScore: {}, homeScore: {}",
                awayName, homeName, awayScore, homeScore);

        Element boxScoreAnchor = query(scoreboard, selectors.getBoxScoreLink());
        String boxScoreUrl = boxScoreAnchor != null ? resolveUrl(boxScoreAnchor.attr("href")) : null;

        Element table = query(scoreboard, selectors.getScoreTable().getTable());
        Map<String, KboScoreboardTeam> tableScores = parseTableScores(table);

        KboScoreboardTeam awayTeam = mergeTeamData(awayName, awayScore, tableScores);
        KboScoreboardTeam homeTeam = mergeTeamData(homeName, homeScore, tableScores);

        if (awayTeam == null && homeTeam == null) {
            log.warn("스코어보드 파싱 실패: 팀 정보를 찾을 수 없습니다.");
            return Optional.empty();
        }

        Pitcher pitcher = parsePitcher(scoreboard);

        return Optional.of(new KboScoreboardGame(
                date,
                emptyToNull(status),
                emptyToNull(stadium),
                parseLocalTimeEmptyToNull(startTime),
                emptyToNull(boxScoreUrl),
                awayTeam,
                homeTeam,
                awayScore,
                homeScore,
                pitcher.winning(),
                pitcher.saving(),
                pitcher.losing()
        ));
    }

    private String cleanStadiumName(final String stadium, final String startTime) {
        if (stadium != null && startTime != null) {
            return stadium.replace(startTime, "").trim();
        }
        return stadium != null ? stadium.trim() : null;
    }

    private Map<String, KboScoreboardTeam> parseTableScores(final Element table) {
        Map<String, KboScoreboardTeam> scores = new LinkedHashMap<>();
        if (table == null) {
            return scores;
        }

        var tableSelectors = properties.getSelectors().getScoreboard().getScoreTable();
        Elements rows = table.select(tableSelectors.getRows());

        for (Element row : rows) {
            String teamName = safeText(row, tableSelectors.getTeamName());
            if (teamName == null || teamName.isBlank()) {
                continue;
            }

            Elements cells = row.select(tableSelectors.getCells());
            if (cells.isEmpty()) {
                continue;
            }

            int size = cells.size();
            int statsStart = Math.max(size - 4, 0);

            List<String> inningScores = new ArrayList<>();
            for (int i = 0; i < statsStart; i++) {
                inningScores.add(normalizeScore(cells.get(i).text()));
            }

            Integer runs = statsStart < size ? parseNullableInt(cells.get(size - 4).text()) : null;
            Integer hits = statsStart < size ? parseNullableInt(cells.get(size - 3).text()) : null;
            Integer errors = statsStart < size ? parseNullableInt(cells.get(size - 2).text()) : null;
            Integer bases = statsStart < size ? parseNullableInt(cells.get(size - 1).text()) : null;

            scores.put(teamName.trim(), new KboScoreboardTeam(
                    teamName.trim(),
                    runs,
                    hits,
                    errors,
                    bases,
                    inningScores
            ));
        }

        return scores;
    }

    private KboScoreboardTeam mergeTeamData(
            final String teamName,
            final Integer displayScore,
            final Map<String, KboScoreboardTeam> tableScores) {

        if (tableScores.isEmpty() && teamName == null) {
            return null;
        }

        KboScoreboardTeam tableTeam = null;
        if (teamName != null) {
            tableTeam = tableScores.remove(teamName.trim());
        }

        if (tableTeam == null && !tableScores.isEmpty()) {
            String firstKey = tableScores.keySet().iterator().next();
            tableTeam = tableScores.remove(firstKey);
        }

        if (tableTeam == null) {
            if (teamName == null && displayScore == null) {
                return null;
            }
            return new KboScoreboardTeam(
                    teamName,
                    displayScore,
                    null,
                    null,
                    null,
                    List.of()
            );
        }

        Integer runs = tableTeam.runs() != null ? tableTeam.runs() : displayScore;
        return new KboScoreboardTeam(
                teamName != null ? teamName : tableTeam.name(),
                runs,
                tableTeam.hits(),
                tableTeam.errors(),
                tableTeam.basesOnBalls(),
                tableTeam.inningScores()
        );
    }

    private Pitcher parsePitcher(final Element scoreboard) {
        var pitcherSelectors = properties.getSelectors().getScoreboard().getPitcher();

        String win = null;
        String save = null;
        String lose = null;
        Element container = query(scoreboard, pitcherSelectors.getContainer());
        if (container != null) {
            Elements spans = container.select(pitcherSelectors.getSpans());
            for (Element span : spans) {
                String raw = span.text();
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                raw = raw.replace('\u00A0', ' ').trim();
                Matcher matcher = pitcherPattern.matcher(raw);
                if (!matcher.find()) {
                    continue;
                }

                String label = matcher.group(1);
                String name = matcher.group(2).trim();
                if (name.isEmpty() || "-".equals(name)) {
                    continue;
                }

                switch (label) {
                    case "승" -> win = name;
                    case "세" -> save = name;
                    case "패" -> lose = name;
                }
            }
        }
        return new Pitcher(win, save, lose);
    }

    private String safeText(final Element parent, final String selector) {
        Element element = query(parent, selector);
        if (element == null) {
            return null;
        }
        String text = element.text();
        return text != null ? text.trim() : null;
    }

    private Element query(final Element parent, final String selector) {
        if (parent == null || selector == null) {
            return null;
        }
        return parent.selectFirst(selector);
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

    private String normalizeScore(final String text) {
        return text == null ? "" : text.trim();
    }

    private String resolveUrl(final String rawUrl) {
        String baseUrl = properties.getCrawler().getBaseUrl();
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl;
        }
        if (rawUrl.startsWith("//")) {
            return "https:" + rawUrl;
        }
        if (rawUrl.startsWith("/")) {
            return baseUrl + rawUrl;
        }
        return baseUrl + rawUrl;
    }

    private LocalTime parseLocalTimeEmptyToNull(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : parseTime(trimmed);
    }

    private LocalTime parseTime(final String startTime) {
        try {
            return LocalTime.parse(startTime, timeFormatter);
        } catch (Exception e) {
            throw new GameSyncException("Invalid time format: " + startTime);
        }
    }

    private record Pitcher(String winning, String saving, String losing) {
    }
}

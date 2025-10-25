package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.dto.Pitcher;

@Slf4j
@RequiredArgsConstructor
public class KboScoreboardCrawler {

    private final KboCrawlerProperties properties;
    private final PlaywrightManager pwManager;
    private final Pattern pitcherPattern;
    private final DateTimeFormatter labelFormatter;
    private final DateTimeFormatter timeFormatter;

    public KboScoreboardCrawler(KboCrawlerProperties properties, PlaywrightManager pwManager) {
        this.properties = properties;
        this.pwManager = pwManager;
        this.pitcherPattern = Pattern.compile(properties.getPatterns().getPitcherLabel());
        this.labelFormatter = DateTimeFormatter.ofPattern(properties.getPatterns().getDateFormat());
        this.timeFormatter = DateTimeFormatter.ofPattern(properties.getPatterns().getTimeFormat());
    }

    public synchronized Map<LocalDate, List<KboScoreboardGame>> crawl(final List<LocalDate> dates) {
        Map<LocalDate, List<KboScoreboardGame>> result = new LinkedHashMap<>();
        if (dates == null || dates.isEmpty()) {
            return result;
        }

        for (LocalDate date : dates) {
            log.debug("조회 날짜: {}", date);

            int maxRetries = 3;
            boolean success = false;

            for (int attempt = 1; attempt <= maxRetries && !success; attempt++) {
                try {
                    List<KboScoreboardGame> games = pwManager.withPage(page -> {
                        String baseUrl = properties.getCrawler().getScoreBoardUrl();
                        long navTimeout = properties.getCrawler().getNavigationTimeout().toMillis();

                        page.navigate(baseUrl, new Page.NavigateOptions().setTimeout(navTimeout));
                        navigateToDateUsingCalendar(page, date);
                        return extractScoreboards(page, date);
                    });

                    result.put(date, games);
                    success = true;

                } catch (PlaywrightException e) {
                    log.warn("날짜 {} 크롤링 실패 (시도 {}/{}): {}", date, attempt, maxRetries, e.getMessage());

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }

            if (!success) {
                log.error("❌ 날짜 {} 크롤링 최종 실패 - 해당 날짜 데이터 없음", date);
            }
        }

        return result;
    }

    private void navigateToDateUsingCalendar(final Page page, final LocalDate targetDate) {
        long timeout = properties.getCrawler().getWaitTimeout().toMillis();
        var calendarSelectors = properties.getSelectors().getCalendar();

        // 1) 달력 열기 - XPath
        page.locator(calendarSelectors.getTrigger())
                .click(new Locator.ClickOptions().setTimeout(timeout));

        page.locator(calendarSelectors.getContainer())
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(timeout)
                        .setState(WaitForSelectorState.VISIBLE));

        // 2) 연/월 선택 - XPath
        String year = String.valueOf(targetDate.getYear());
        String monthZeroBased = String.valueOf(targetDate.getMonthValue() - 1);

        page.locator(calendarSelectors.getYearSelect())
                .selectOption(year);
        page.locator(calendarSelectors.getMonthSelect())
                .selectOption(monthZeroBased);

        // 3) 일자 클릭 - XPath (텍스트 기반)
        String day = String.valueOf(targetDate.getDayOfMonth());
        String dayXpath = String.format(calendarSelectors.getDayLink(), day);

        Locator dayLocator = page.locator(dayXpath);
        dayLocator.waitFor(new Locator.WaitForOptions()
                .setTimeout(timeout)
                .setState(WaitForSelectorState.VISIBLE));
        dayLocator.click(new Locator.ClickOptions().setTimeout(timeout));

        // 4) 라벨 변경 대기 - XPath
        String expected = labelFormatter.format(targetDate);

        try {
            page.locator(calendarSelectors.getDateLabel())
                    .filter(new Locator.FilterOptions().setHasText(expected))
                    .waitFor(new Locator.WaitForOptions()
                            .setTimeout(timeout)
                            .setState(WaitForSelectorState.VISIBLE));
        } catch (PlaywrightException e) {
            // Fallback: JavaScript 대기
            page.waitForFunction("(args) => {" +
                            "  const [exp, sel] = args;" +
                            "  const xpath = document.evaluate(sel, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);"
                            +
                            "  const el = xpath.singleNodeValue;" +
                            "  return el && el.textContent && el.textContent.includes(exp);" +
                            "}",
                    new Object[]{expected, calendarSelectors.getDateLabel()},
                    new Page.WaitForFunctionOptions().setTimeout(timeout));
        }

        // 5) 스코어 요소 등장 대기 - XPath
        page.locator(calendarSelectors.getUpdatePanel())
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(timeout)
                        .setState(WaitForSelectorState.ATTACHED));
    }

    private boolean waitForScoreboardsOrSkip(final Page page) {
        long timeout = properties.getCrawler().getWaitTimeout().toMillis();
        String containerSelector = properties.getSelectors().getScoreboard().getContainer();

        try {
            page.waitForSelector(containerSelector,
                    new Page.WaitForSelectorOptions()
                            .setTimeout(timeout)
                            .setState(WaitForSelectorState.ATTACHED));
            return true;
        } catch (PlaywrightException ignore) {
            return false;
        }
    }

    private List<KboScoreboardGame> extractScoreboards(final Page page, final LocalDate date) {
        if (!waitForScoreboardsOrSkip(page)) {
            return List.of();
        }

        String containerSelector = properties.getSelectors().getScoreboard().getContainer();
        List<ElementHandle> scoreboards = page.querySelectorAll(containerSelector);

        if (scoreboards.isEmpty()) {
            log.info("스코어보드가 존재하지 않습니다.");
            return List.of();
        }

        List<KboScoreboardGame> games = new ArrayList<>();
        for (ElementHandle scoreboard : scoreboards) {
            Optional<KboScoreboardGame> parsed = parseScoreboard(scoreboard, date);
            parsed.ifPresent(games::add);
        }

        return games;
    }

    private Optional<KboScoreboardGame> parseScoreboard(final ElementHandle scoreboard, final LocalDate date) {
        var selectors = properties.getSelectors().getScoreboard();

        // ✅ 디버깅: 선택자 값 확인
        log.debug("[DEBUG] status selector: {}", selectors.getStatus());
        log.debug("[DEBUG] stadium selector: {}", selectors.getStadium());
        log.debug("[DEBUG] awayTeam name selector: {}",
                selectors.getAwayTeam() != null ? selectors.getAwayTeam().getName() : "NULL");

        // ✅ YML 설정의 CSS Selector 사용
        String status = safeTextCSS(scoreboard, selectors.getStatus());
        String stadium = safeTextCSS(scoreboard, selectors.getStadium());
        String startTime = safeTextCSS(scoreboard, selectors.getStartTime());

        log.debug("[DEBUG] Parsed - status: {}, stadium: {}, startTime: {}", status, stadium, startTime);

        if (stadium != null && startTime != null) {
            stadium = stadium.replace(startTime, "").trim();
        } else if (stadium != null) {
            stadium = stadium.trim();
        }

        String awayName = safeTextCSS(scoreboard, selectors.getAwayTeam().getName());
        String homeName = safeTextCSS(scoreboard, selectors.getHomeTeam().getName());
        Integer awayScore = parseNullableInt(safeTextCSS(scoreboard, selectors.getAwayTeam().getScore()));
        Integer homeScore = parseNullableInt(safeTextCSS(scoreboard, selectors.getHomeTeam().getScore()));

        log.debug("[DEBUG] Parsed - awayName: {}, homeName: {}, awayScore: {}, homeScore: {}",
                awayName, homeName, awayScore, homeScore);

        ElementHandle boxScoreAnchor = queryCSS(scoreboard, selectors.getBoxScoreLink());
        String boxScoreUrl = boxScoreAnchor != null ? resolveUrl(boxScoreAnchor.getAttribute("href")) : null;

        ElementHandle table = queryCSS(scoreboard, selectors.getScoreTable().getTable());
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

    private Map<String, KboScoreboardTeam> parseTableScores(final ElementHandle table) {
        Map<String, KboScoreboardTeam> scores = new LinkedHashMap<>();
        if (table == null) {
            return scores;
        }

        // ✅ YML 설정 사용
        var tableSelectors = properties.getSelectors().getScoreboard().getScoreTable();
        List<ElementHandle> rows = table.querySelectorAll(tableSelectors.getRows());

        for (ElementHandle row : rows) {
            String teamName = safeTextCSS(row, tableSelectors.getTeamName());
            if (teamName == null || teamName.isBlank()) {
                continue;
            }

            List<ElementHandle> cells = row.querySelectorAll(tableSelectors.getCells());
            if (cells.isEmpty()) {
                continue;
            }

            int size = cells.size();
            int statsStart = Math.max(size - 4, 0);
            List<String> inningScores = new ArrayList<>();
            for (int i = 0; i < statsStart; i++) {
                inningScores.add(normalizeScore(cells.get(i).innerText()));
            }

            Integer runs = statsStart < size ? parseNullableInt(cells.get(size - 4).innerText()) : null;
            Integer hits = statsStart < size ? parseNullableInt(cells.get(size - 3).innerText()) : null;
            Integer errors = statsStart < size ? parseNullableInt(cells.get(size - 2).innerText()) : null;
            Integer bases = statsStart < size ? parseNullableInt(cells.get(size - 1).innerText()) : null;

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

    private KboScoreboardTeam mergeTeamData(final String teamName,
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

    private String safeTextCSS(final ElementHandle parent, final String selector) {
        if (parent == null || selector == null) {
            return null;
        }
        try {
            ElementHandle element = parent.querySelector(selector);
            if (element == null) {
                return null;
            }
            String text = element.innerText();
            return text != null ? text.trim() : null;
        } catch (PlaywrightException exception) {
            return null;
        }
    }

    // ✅ CSS Selector 단일 요소 조회
    private ElementHandle queryCSS(final ElementHandle parent, final String selector) {
        if (parent == null || selector == null) {
            return null;
        }
        try {
            return parent.querySelector(selector);
        } catch (PlaywrightException e) {
            return null;
        }
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

    private String normalizeScore(final String text) {
        return text == null ? "" : text.trim();
    }

    private String resolveUrl(final String rawUrl) {
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
            return "https://www.koreabaseball.com" + rawUrl;
        }
        return "https://www.koreabaseball.com/" + rawUrl;
    }

    private String emptyToNull(final String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private LocalTime parseLocalTimeEmptyToNull(final String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : parseTime(t);
    }

    private Pitcher parsePitcher(ElementHandle scoreboard) {
        var pitcherSelectors = properties.getSelectors().getScoreboard().getPitcher();

        String win = null, save = null, lose = null;
        try {
            // ✅ YML 설정 사용
            ElementHandle container = scoreboard.querySelector(pitcherSelectors.getContainer());
            if (container != null) {
                List<ElementHandle> spans = container.querySelectorAll(pitcherSelectors.getSpans());
                for (ElementHandle s : spans) {
                    String raw = s.innerText();
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    raw = raw.replace('\u00A0', ' ').trim();
                    Matcher m = pitcherPattern.matcher(raw);
                    if (!m.find()) {
                        continue;
                    }

                    String label = m.group(1);
                    String name = m.group(2).trim();
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
        } catch (PlaywrightException ignored) {
        }
        return new Pitcher(win, save, lose);
    }

    private LocalTime parseTime(String startTime) {
        try {
            return LocalTime.parse(startTime, timeFormatter);
        } catch (Exception e) {
            throw new GameSyncException("Invalid time format: " + startTime);
        }
    }
}

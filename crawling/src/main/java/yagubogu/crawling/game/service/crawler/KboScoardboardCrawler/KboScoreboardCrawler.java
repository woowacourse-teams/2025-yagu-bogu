package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.dto.Pitcher;

@Slf4j
@RequiredArgsConstructor
public class KboScoreboardCrawler {

    private static final DateTimeFormatter LABEL_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern LABELED = Pattern.compile("^\\s*(승|세|패)\\s*[:：]\\s*(.+?)\\s*$");

    private final String baseUrl;
    private final Duration navigationTimeout;
    private final Duration waitTimeout;
    private final PlaywrightManager pwManager;

    public synchronized Map<LocalDate, List<KboScoreboardGame>> crawlManyScoreboard(final List<LocalDate> dates) {
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
                        page.navigate(baseUrl, new Page.NavigateOptions().setTimeout(navigationTimeout.toMillis()));
                        navigateToDateUsingCalendar(page, date, waitTimeout);
                        return extractScoreboards(page, date);
                    });

                    result.put(date, games);
                    success = true;

                } catch (PlaywrightException e) {
                    log.warn("날짜 {} 크롤링 실패 (시도 {}/{}): {}", date, attempt, maxRetries, e.getMessage());

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(2000);  // Browser 재연결 대기
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

    private void navigateToDateUsingCalendar(final Page page, final LocalDate targetDate,
                                             final Duration waitTimeout) {
        // 1) 달력 열기
        page.locator(".ui-datepicker-trigger")
                .click(new Locator.ClickOptions().setTimeout(waitTimeout.toMillis()));

        page.locator("#ui-datepicker-div")
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(waitTimeout.toMillis())
                        .setState(WaitForSelectorState.VISIBLE));

        // 2) 연/월 선택
        String year = String.valueOf(targetDate.getYear());
        String monthZeroBased = String.valueOf(targetDate.getMonthValue() - 1);

        page.locator(".ui-datepicker-year")
                .selectOption(year);
        page.locator(".ui-datepicker-month")
                .selectOption(monthZeroBased);

        // ✅ 3) 일자 클릭 - Locator 사용 + 안전한 대기
        String day = String.valueOf(targetDate.getDayOfMonth());
        String dayXpath = String.format(
                "//div[@id='ui-datepicker-div']//td[not(contains(@class,'ui-datepicker-other-month'))]//a[normalize-space(text())='%s']",
                day);

        Locator dayLocator = page.locator(dayXpath);

        // ✅ 클릭 가능 상태 확인
        dayLocator.waitFor(new Locator.WaitForOptions()
                .setTimeout(waitTimeout.toMillis())
                .setState(WaitForSelectorState.VISIBLE));

        // ✅ 클릭
        dayLocator.click(new Locator.ClickOptions().setTimeout(waitTimeout.toMillis()));

        // ✅ 4) 라벨 변경 대기 - 더 안정적인 방식
        String expected = LABEL_FMT.format(targetDate);

        try {
            page.locator("#cphContents_cphContents_cphContents_lblGameDate")
                    .filter(new Locator.FilterOptions().setHasText(expected))
                    .waitFor(new Locator.WaitForOptions()
                            .setTimeout(waitTimeout.toMillis())
                            .setState(WaitForSelectorState.VISIBLE));
        } catch (PlaywrightException e) {
            // fallback: waitForFunction 사용
            page.waitForFunction("(args) => {" +
                            "  const [exp, sel] = args;" +
                            "  const el = document.querySelector(sel);" +
                            "  return el && el.textContent && el.textContent.includes(exp);" +
                            "}",
                    new Object[]{expected, "#cphContents_cphContents_cphContents_lblGameDate"},
                    new Page.WaitForFunctionOptions().setTimeout(waitTimeout.toMillis()));
        }

        // ✅ 5) 스코어 요소 등장 대기
        page.locator("#cphContents_cphContents_cphContents_udpRecord")
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(waitTimeout.toMillis())
                        .setState(WaitForSelectorState.ATTACHED));
    }

    private boolean waitForScoreboardsOrSkip(final Page page,
                                             final Duration timeout) {
        try {
            page.waitForSelector(".smsScore",
                    new Page.WaitForSelectorOptions().setTimeout(timeout.toMillis())
                            .setState(WaitForSelectorState.ATTACHED));
            return true;
        } catch (PlaywrightException ignore) {
            return false;
        }
    }

    private List<KboScoreboardGame> extractScoreboards(final Page page, final LocalDate date) {
        if (!waitForScoreboardsOrSkip(page, waitTimeout)) {
            return List.of();
        }

        List<ElementHandle> scoreboards = page.querySelectorAll(".smsScore");
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
        String status = safeText(scoreboard, ".flag span");
        String stadium = safeText(scoreboard, ".place");
        String startTime = safeText(scoreboard, ".place span");
        if (stadium != null && startTime != null) {
            stadium = stadium.replace(startTime, "").trim();
        } else if (stadium != null) {
            stadium = stadium.trim();
        }

        String awayName = safeText(scoreboard, ".leftTeam .teamT");
        String homeName = safeText(scoreboard, ".rightTeam .teamT");
        Integer awayScore = parseNullableInt(safeText(scoreboard, ".leftTeam .score span"));
        Integer homeScore = parseNullableInt(safeText(scoreboard, ".rightTeam .score span"));

        ElementHandle boxScoreAnchor = scoreboard.querySelector(".btnSms a[href*='gameId=']");
        String boxScoreUrl = boxScoreAnchor != null ? resolveUrl(boxScoreAnchor.getAttribute("href")) : null;
        String gameId = extractGameId(boxScoreUrl);

        ElementHandle table = scoreboard.querySelector("table.tScore");
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
                gameId,
                emptyToNull(status),
                emptyToNull(stadium),
                emptyToNull(startTime),
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

        List<ElementHandle> rows = table.querySelectorAll("tbody tr");
        for (ElementHandle row : rows) {
            String teamName = safeText(row, "th");
            if (teamName == null || teamName.isBlank()) {
                continue;
            }

            List<ElementHandle> cells = row.querySelectorAll("td");
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

    private String safeText(final ElementHandle parent, final String selector) {
        if (parent == null) {
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

    private void sleep(final Duration delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay.toMillis());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
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

    private String extractGameId(final String boxScoreUrl) {
        if (boxScoreUrl == null) {
            return null;
        }
        int index = boxScoreUrl.indexOf("gameId=");
        if (index < 0) {
            return null;
        }
        String substring = boxScoreUrl.substring(index + "gameId=".length());
        int ampIndex = substring.indexOf('&');
        if (ampIndex >= 0) {
            substring = substring.substring(0, ampIndex);
        }
        return substring;
    }

    private String emptyToNull(final String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private Pitcher parsePitcher(ElementHandle scoreboard) {
        String win = null, save = null, lose = null;
        try {
            ElementHandle container = scoreboard.querySelector(".score_wrap p.win");
            if (container != null) {
                for (ElementHandle s : container.querySelectorAll("span")) {
                    String raw = s.innerText();
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    raw = raw.replace('\u00A0', ' ').trim();
                    Matcher m = LABELED.matcher(raw);
                    if (!m.find()) {
                        continue;
                    }

                    String label = m.group(1); // 승|세|패
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
}

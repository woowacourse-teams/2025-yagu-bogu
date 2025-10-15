package com.yagubogu.game.service.crawler.KboScoardboardCrawler;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.yagubogu.game.dto.KboScoreboardGame;
import com.yagubogu.game.dto.KboScoreboardTeam;
import com.yagubogu.game.dto.Pitcher;
import com.yagubogu.game.service.crawler.manager.PlaywrightManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@RequiredArgsConstructor
public class KboScoreboardCrawler {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(KboScoreboardCrawler.class);
    private static final String BASE_URL = "https://www.koreabaseball.com/Schedule/ScoreBoard.aspx";
    private static final DateTimeFormatter LABEL_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern LABELED =
            Pattern.compile("^\\s*(승|세|패)\\s*[:：]\\s*(.+?)\\s*$");

    private final Duration navigationTimeout;
    private final Duration waitTimeout;
    private final PlaywrightManager pwManager;

    public synchronized Map<LocalDate, List<KboScoreboardGame>> crawlManyScoreboard(final List<LocalDate> dates) {
        Map<LocalDate, List<KboScoreboardGame>> result = new LinkedHashMap<>();
        if (dates == null || dates.isEmpty()) {
            return result;
        }

        Page page = null;
        try {
            page = pwManager.acquirePage();
            page.navigate(BASE_URL, new Page.NavigateOptions().setTimeout(navigationTimeout.toMillis()));

            for (LocalDate date : dates) {
                log.debug("조회 날짜: {}", date);
                navigateToDateUsingCalendar(page, date, waitTimeout);
                List<KboScoreboardGame> games = extractScoreboards(page, DEFAULT_LOGGER, date);
                result.put(date, games);
            }
            return result;
        } finally {
            if (page != null) {
                pwManager.releasePage(page);
            }
        }
    }

    private void navigateToDateUsingCalendar(final Page page, final LocalDate targetDate,
                                             final Duration waitTimeout) {
        // 1) 달력 열기
        page.click(".ui-datepicker-trigger", new Page.ClickOptions()
                .setTimeout(waitTimeout.toMillis()));
        page.waitForSelector("#ui-datepicker-div",
                new Page.WaitForSelectorOptions().setTimeout(waitTimeout.toMillis())
                        .setState(WaitForSelectorState.VISIBLE));

        // 2) 연/월 선택 (월은 0-based)
        String year = String.valueOf(targetDate.getYear());
        String monthZeroBased = String.valueOf(targetDate.getMonthValue() - 1);
        page.selectOption(".ui-datepicker-year", new SelectOption().setValue(year));
        page.selectOption(".ui-datepicker-month", new SelectOption().setValue(monthZeroBased));

        // 3) 일자 클릭 (다른 달 날짜 제외)
        String day = String.valueOf(targetDate.getDayOfMonth());
        // XPath가 가장 안전
        String dayXpath = String.format(
                "//div[@id='ui-datepicker-div']//td[not(contains(@class,'ui-datepicker-other-month'))]//a[normalize-space(text())='%s']",
                day);
        page.click(dayXpath, new Page.ClickOptions().setTimeout(waitTimeout.toMillis()));

        // 4) 라벨로 로딩 완료 확인 (예: 2025.10.04)
        String expected = LABEL_FMT.format(targetDate);
        page.waitForFunction("(args) => {" +
                        "  const [exp, sel] = args;" +
                        "  const el = document.querySelector(sel);" +
                        "  return el && el.textContent && el.textContent.includes(exp);" +
                        "}",
                new Object[]{expected, "#cphContents_cphContents_cphContents_lblGameDate"},
                new Page.WaitForFunctionOptions().setTimeout(waitTimeout.toMillis()));

        // 5) 스코어 요소 등장 대기
        page.waitForSelector("#cphContents_cphContents_cphContents_udpRecord",
                new Page.WaitForSelectorOptions().setTimeout(waitTimeout.toMillis())
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

    private List<KboScoreboardGame> extractScoreboards(final Page page, final Logger logger, final LocalDate date) {
        if (!waitForScoreboardsOrSkip(page, waitTimeout)) {
            return List.of();
        }

        List<ElementHandle> scoreboards = page.querySelectorAll(".smsScore");
        if (scoreboards.isEmpty()) {
            logger.info("스코어보드가 존재하지 않습니다.");
            return List.of();
        }

        List<KboScoreboardGame> games = new ArrayList<>();
        for (ElementHandle scoreboard : scoreboards) {
            Optional<KboScoreboardGame> parsed = parseScoreboard(scoreboard, logger, date);
            parsed.ifPresent(games::add);
        }

        return games;
    }

    private Optional<KboScoreboardGame> parseScoreboard(final ElementHandle scoreboard,
                                                        final Logger logger, final LocalDate date) {
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
            logger.warn("스코어보드 파싱 실패: 팀 정보를 찾을 수 없습니다.");
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

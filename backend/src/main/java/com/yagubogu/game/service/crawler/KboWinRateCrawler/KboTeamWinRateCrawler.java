package com.yagubogu.game.service.crawler.KboWinRateCrawler;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.yagubogu.game.dto.TeamWinRateRow;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KboTeamWinRateCrawler {

    private static final String TEAM_RANKING_URL = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
    private static final String TABLE_SELECTOR = ".tData";
    private static final Duration NAVIGATION_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration TABLE_TIMEOUT = Duration.ofSeconds(60);
    private static final int MAX_RETRY = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    private static final String SERIES_SELECT = "#cphContents_cphContents_cphContents_ddlSeries";
    private static final String SERIES_HIDDEN = "#cphContents_cphContents_cphContents_hfSearchSeries";

    public List<TeamWinRateRow> crawl(final Set<String> validTeams,
                                      final SeriesType seriesType
    ) {
        Objects.requireNonNull(validTeams, "validTeams");

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {

                Page page = browser.newPage();
                page.navigate(TEAM_RANKING_URL, new Page.NavigateOptions().setTimeout(NAVIGATION_TIMEOUT.toMillis()));
                page.waitForSelector(TABLE_SELECTOR,
                        new Page.WaitForSelectorOptions()
                                .setTimeout(TABLE_TIMEOUT.toMillis())
                                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

                page.selectOption(SERIES_SELECT, seriesType.value());
                page.waitForFunction(
                        "arg => { " +
                                "  const [sel, hid, val] = arg; " +
                                "  const s = document.querySelector(sel); " +
                                "  const h = document.querySelector(hid); " +
                                "  return s && h && s.value === val && h.value === val;" +
                                "}",
                        new Object[]{SERIES_SELECT, SERIES_HIDDEN, seriesType.value()},
                        new Page.WaitForFunctionOptions().setTimeout(TABLE_TIMEOUT.toMillis())
                );

                page.waitForSelector(TABLE_SELECTOR,
                        new Page.WaitForSelectorOptions()
                                .setTimeout(TABLE_TIMEOUT.toMillis())
                                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
                page.waitForFunction(
                        "sel => document.querySelectorAll(sel + ' tbody tr').length > 0",
                        TABLE_SELECTOR);

                List<ElementHandle> rows = page.querySelectorAll(TABLE_SELECTOR + " tr");
                if (rows.isEmpty()) {
                    log.warn("팀별 승률 테이블을 찾을 수 없습니다.");
                    return List.of();
                }

                List<TeamWinRateRow> winRates = new ArrayList<>();
                for (int index = 1; index < rows.size(); index++) {
                    ElementHandle row = rows.get(index);
                    List<ElementHandle> columns = row.querySelectorAll("td");
                    if (columns.size() < 7) {
                        continue;
                    }

                    String team = columns.get(1).innerText().trim();
                    if (!isValidTeam(team, validTeams)) {
                        continue;
                    }

                    int totalPlays = Integer.parseInt(columns.get(2).innerText().trim());
                    int winCounts = Integer.parseInt(columns.get(3).innerText().trim());
                    int loseCounts = Integer.parseInt(columns.get(4).innerText().trim());
                    int drawCounts = Integer.parseInt(columns.get(5).innerText().trim());

                    String winRateText = columns.get(6).innerText().trim();
                    BigDecimal winRate = parseWinRate(winRateText);
                    double diffRate = Double.parseDouble(columns.get(7).innerText().trim());
                    String latestPlays = columns.get(8).innerText().trim();
                    String streak = columns.get(9).innerText().trim();
                    String home = columns.get(10).innerText().trim();
                    String away = columns.get(11).innerText().trim();

                    winRates.add(new TeamWinRateRow(index, team, totalPlays, winCounts, loseCounts, drawCounts, winRate,
                            diffRate, latestPlays, streak, home, away));
                }

                log.info("팀별 승률 크롤링 완료: {}건", winRates.size());

                return winRates;
            } catch (PlaywrightException exception) {
                log.error("팀별 승률 크롤링 중 오류 발생(시도 {}/{}): {}", attempt, MAX_RETRY, exception.getMessage());
                if (attempt == MAX_RETRY) {
                    break;
                }
                sleep(RETRY_DELAY);
            }
        }

        return List.of();
    }

    private boolean isValidTeam(final String team, final Set<String> validTeams) {
        if (team == null || team.isBlank()) {
            return false;
        }
        if (validTeams.isEmpty()) {
            return true;
        }
        String trimmed = team.trim();
        String normalized = trimmed.replace(" ", "");

        return validTeams.contains(trimmed)
                || validTeams.contains(normalized);
    }

    private BigDecimal parseWinRate(final String winRateText) {
        if (winRateText == null) {
            return BigDecimal.ZERO;
        }

        String sanitized = winRateText.replaceAll("[^0-9.]", "");
        if (sanitized.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(sanitized);
        } catch (NumberFormatException exception) {
            log.warn("승률 값을 파싱할 수 없습니다: {}", winRateText);
            return BigDecimal.ZERO;
        }
    }

    private void sleep(final Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}


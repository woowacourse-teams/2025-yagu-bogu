package yagubogu.crawling.game.service.crawler.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.config.KboCrawlerProperties.CalendarSelectors;

@Slf4j
public abstract class BaseKboPage {

    protected final Page page;
    protected final KboCrawlerProperties properties;

    protected BaseKboPage(Page page, KboCrawlerProperties properties) {
        this.page = page;
        this.properties = properties;
    }

    // ==================== 추상 메서드 ====================

    protected abstract String getBaseUrl();

    /**
     * 각 페이지별 콘텐츠 업데이트 대기 로직
     */
    protected abstract void waitForContentUpdate(long timeout);

    /**
     * 날짜 변경 확인 필요 여부 (스코어보드: true, 게임센터: false)
     */
    protected boolean needsDateChangeValidation() {
        return false; // 기본값: 불필요
    }

    // ==================== 공통 네비게이션 ====================

    public void navigateTo() {
        log.info("{}로 이동 중...", getPageName());

        long timeout = properties.getCrawler().getNavigationTimeout().toMillis();

        page.navigate(getBaseUrl(), new Page.NavigateOptions()
                .setTimeout(timeout)
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
        );

        log.info("페이지 로딩 완료");
    }

    /**
     * 특정 날짜로 이동
     */
    public void navigateToDate(LocalDate targetDate) {
        log.info("날짜 {}로 이동 중...", targetDate);

        long timeout = properties.getCrawler().getWaitTimeout().toMillis();
        var calendarSelectors = properties.getSelectors().getCalendar();

        openCalendar(timeout, calendarSelectors);
        selectYearMonth(targetDate, timeout, calendarSelectors);
        clickDay(targetDate, timeout, calendarSelectors);

        // 날짜 변경 확인이 필요한 페이지만 실행
        if (needsDateChangeValidation()) {
            waitForDateChange(targetDate, timeout, calendarSelectors);
        }

        waitForContentUpdate(timeout);

        log.info("날짜 이동 완료");
    }

    // ==================== 달력 조작 (공통) ====================

    private void openCalendar(long timeout, CalendarSelectors calendarSelectors) {
        page.locator(calendarSelectors.getTrigger())
                .click(new Locator.ClickOptions().setTimeout(timeout));

        page.locator(calendarSelectors.getContainer())
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(timeout)
                        .setState(WaitForSelectorState.VISIBLE));
    }

    private void selectYearMonth(LocalDate targetDate, long timeout, CalendarSelectors calendarSelectors) {
        String year = String.valueOf(targetDate.getYear());
        String monthZeroBased = String.valueOf(targetDate.getMonthValue() - 1);

        page.locator(calendarSelectors.getYearSelect())
                .selectOption(year);
        page.locator(calendarSelectors.getMonthSelect())
                .selectOption(monthZeroBased);

        log.debug("년/월 선택 완료: {}-{}", targetDate.getYear(), targetDate.getMonthValue());
    }

    private void clickDay(LocalDate targetDate, long timeout, CalendarSelectors calendarSelectors) {
        String day = String.valueOf(targetDate.getDayOfMonth());
        String dayXpath = String.format(calendarSelectors.getDayLink(), day);

        Locator dayLocator = page.locator(dayXpath);

        if (dayLocator.count() == 0) {
            throw new RuntimeException("날짜를 찾을 수 없습니다: " + day);
        }

        dayLocator.waitFor(new Locator.WaitForOptions()
                .setTimeout(timeout)
                .setState(WaitForSelectorState.VISIBLE));

        dayLocator.first().click(new Locator.ClickOptions()
                .setTimeout(timeout)
                .setForce(false)
        );

        log.debug("날짜 {} 클릭 완료", day);
    }

    /**
     * 날짜 라벨 변경 대기 (스코어보드용)
     */
    private void waitForDateChange(LocalDate targetDate, long timeout, CalendarSelectors calendarSelectors) {
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern(
                properties.getPatterns().getDateFormat());
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
                            "  const xpath = document.evaluate(sel, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);" +
                            "  const el = xpath.singleNodeValue;" +
                            "  return el && el.textContent && el.textContent.includes(exp);" +
                            "}",
                    new Object[]{expected, calendarSelectors.getDateLabel()},
                    new Page.WaitForFunctionOptions().setTimeout(timeout));
        }
    }

    // ==================== 공통 유틸리티 ====================

    protected String safeTextCSS(ElementHandle parent, String selector) {
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

    protected ElementHandle queryCSS(ElementHandle parent, String selector) {
        if (parent == null || selector == null) {
            return null;
        }
        try {
            return parent.querySelector(selector);
        } catch (PlaywrightException e) {
            return null;
        }
    }

    protected Integer parseNullableInt(String text) {
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

    protected String emptyToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    protected String normalizeScore(String text) {
        return text == null ? "" : text.trim();
    }

    protected String resolveUrl(String rawUrl) {
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

    protected String getPageName() {
        return this.getClass().getSimpleName();
    }
}

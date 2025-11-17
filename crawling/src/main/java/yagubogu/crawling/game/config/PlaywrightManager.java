package yagubogu.crawling.game.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlaywrightManager {

    private Playwright pw;
    private Browser browser;

    public PlaywrightManager() {
        initializeBrowser();
    }

    private synchronized void initializeBrowser() {
        if (pw != null) {
            try {
                pw.close();
            } catch (Exception ignored) {
            }
        }

        this.pw = Playwright.create();

        // ✅ Chromium → Firefox로 변경
        this.browser = pw.firefox().launch(new BrowserType.LaunchOptions()
//                .setHeadless(false)  // 디버깅용
                .setSlowMo(100)      // 안정성 증가
                .setArgs(List.of(
                        "--disable-gpu",
                        "--no-sandbox"
                )));

        log.info("Firefox 브라우저 초기화 완료");
    }

    private synchronized void ensureBrowserConnected() {
        if (browser == null || !browser.isConnected()) {
            log.warn("Browser 연결 끊김 감지, 재연결 시도...");
            initializeBrowser();
            log.info("Browser 재연결 완료");
        }
    }

    public synchronized <T> T withPage(Function<Page, T> action) {
        ensureBrowserConnected();
        log.info("withPage 시작 - Browser 연결 상태: {}", browser.isConnected());

        Page page = null;
        BrowserContext ctx = null;
        boolean resetBrowser = false;

        try {
            Browser.NewContextOptions opts = new Browser.NewContextOptions()
                    .setViewportSize(1280, 800)
                    .setUserAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setBypassCSP(true);

            ctx = browser.newContext(opts);
            log.info("Context 생성 완료: {}", ctx != null);
            page = ctx.newPage();
            log.info("Page 생성 완료");

            // 리소스 차단
            page.route("**/*", route -> {
                String type = route.request().resourceType();
                if ("image".equals(type) || "media".equals(type) || "font".equals(type)) {
                    route.abort();
                    return;
                }
                route.resume();
            });

            // 타임아웃 증가
            page.setDefaultTimeout(30000);
            page.setDefaultNavigationTimeout(60000);

            return action.apply(page);

        } catch (PlaywrightException e) {
            log.error("withPage 실행 중 Playwright 에러", e);
            if (shouldResetBrowser(e)) {
                log.warn("Playwright 세션이 종료되어 브라우저를 재초기화합니다.");
                resetBrowser = true;
            }
            throw e;
        } catch (RuntimeException e) {
            log.error("withPage 실행 중 런타임 에러", e);
            throw e;
        } catch (Exception e) {
            log.error("withPage 실행 중 에러", e);
            throw new RuntimeException("크롤링 실패", e);

        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    log.debug("Page close 예외 (무시): {}", e.getMessage());
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    log.debug("Context close 예외 (무시): {}", e.getMessage());
                }
            }
            if (resetBrowser) {
                initializeBrowser();
                log.info("재초기화 완료 - 새 Browser 연결: {}", browser.isConnected());
            }
        }
    }

    private boolean shouldResetBrowser(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                if (message.contains("Target page, context or browser has been closed")
                        || message.contains("Target closed")
                        || message.contains("Object doesn't exist")
                        || message.contains("PipeTransport")
                        || message.contains("Stream closed")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (browser != null) {
                browser.close();
            }
        } catch (Exception e) {
            log.debug("Browser close 예외 (무시): {}", e.getMessage());
        }
        try {
            if (pw != null) {
                pw.close();
            }
        } catch (Exception e) {
            log.debug("Playwright close 예외 (무시): {}", e.getMessage());
        }
    }
}

package com.yagubogu.game.service.crawler.manager;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.yagubogu.game.service.crawler.config.KboCrawlerProperties;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightManager {

    private final KboCrawlerProperties properties;
    private final Playwright pw;
    private final Browser browser;
    private final BlockingQueue<BrowserContext> contexts;

    public PlaywrightManager(final KboCrawlerProperties properties) {
        this.properties = properties;
        this.pw = Playwright.create();
        this.browser = pw.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                        "--disable-gpu",
                        "--disable-extensions",
                        "--no-sandbox",
                        "--disable-setuid-sandbox"
                )));
        int contextPoolSize = properties.getContextPoolSize();
        this.contexts = new ArrayBlockingQueue<>(contextPoolSize);
        for (int i = 0; i < contextPoolSize; i++) {
            Browser.NewContextOptions opts = new Browser.NewContextOptions()
                    .setViewportSize(1280, 800)
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                    .setBypassCSP(true);
            BrowserContext ctx = browser.newContext(opts);
            contexts.add(ctx);
        }
    }

    public Page acquirePage() {
        try {
            BrowserContext ctx = contexts.take();
            Page page = ctx.newPage();

            // 리소스 차단: 이미지/폰트/미디어/스타일 일부
            page.route("**/*", route -> {
                String type = route.request().resourceType();
                // 필요 없는 리소스는 차단
                if ("image".equals(type) || "media".equals(type) || "font".equals(type)) {
                    route.abort();
                    return;
                }
                route.resume();
            });

            // 타임아웃 기본값
            page.setDefaultTimeout(properties.getWaitTimeout().toMillis());
            page.setDefaultNavigationTimeout(properties.getNavigationTimeout().toMillis());
            return page;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to acquire page", e);
        }
    }

    public void releasePage(Page page) {
        if (page == null) {
            return;
        }
        BrowserContext ctx = page.context();
        try {
            page.close();
        } catch (Exception ignore) {
        }
        contexts.offer(ctx);
    }

    @PreDestroy
    public void shutdown() {
        try {
            browser.close();
        } catch (Exception ignore) {
        }
        try {
            pw.close();
        } catch (Exception ignore) {
        }
    }
}

package com.yagubogu.game.service.crawler.manager;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.yagubogu.game.service.crawler.config.KboCrawlerProperties;
import jakarta.annotation.PreDestroy;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightManager {

    private final KboCrawlerProperties properties;
    private final Playwright pw;
    private final Browser browser;

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
    }

    public Page acquirePage() {
        Browser.NewContextOptions opts = new Browser.NewContextOptions()
                .setViewportSize(1280, 800)
                .setUserAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                .setBypassCSP(true);

        BrowserContext ctx = browser.newContext(opts);
        Page page = ctx.newPage();

        page.route("**/*", route -> {
            String type = route.request().resourceType();
            if ("image".equals(type) || "media".equals(type) || "font".equals(type)) {
                route.abort();
                return;
            }
            route.resume();
        });

        page.setDefaultTimeout(properties.getWaitTimeout().toMillis());
        page.setDefaultNavigationTimeout(properties.getNavigationTimeout().toMillis());
        return page;
    }

    public void releasePage(Page page) {
        if (page == null) {
            return;
        }
        BrowserContext ctx = page.context();
        try {
            page.close();
            ctx.close();
        } catch (Exception ignore) {
        }
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

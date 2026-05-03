package yagubogu.crawling.game.service.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import yagubogu.crawling.game.config.KboCrawlerProperties;

@Slf4j
public class KboHtmlClient {

    private static final DateTimeFormatter GAME_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final KboCrawlerProperties properties;
    private final HttpClient httpClient;

    public KboHtmlClient(final KboCrawlerProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getCrawler().getNavigationTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Document fetchGameCenter(final LocalDate date) {
        String url = properties.getCrawler().getGameCenterUrl()
                + "?gameDate=" + GAME_DATE_FORMATTER.format(date);
        return getDocument(url);
    }

    public Document fetchReview(final String gameCode) {
        String url = properties.getCrawler().getGameCenterUrl()
                + "?gameDate=" + gameCode.substring(0, 8)
                + "&gameId=" + gameCode
                + "&section=REVIEW";
        return getDocument(url);
    }

    private Document getDocument(final String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(resolveRequestTimeout())
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("KBO HTML 요청 실패: status=" + response.statusCode() + ", url=" + url);
            }
            return Jsoup.parse(response.body(), url);
        } catch (IOException e) {
            throw new IllegalStateException("KBO HTML 요청 실패: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("KBO HTML 요청 중 인터럽트 발생: " + url, e);
        }
    }

    private Duration resolveRequestTimeout() {
        Duration navigationTimeout = properties.getCrawler().getNavigationTimeout();
        Duration waitTimeout = properties.getCrawler().getWaitTimeout();
        return navigationTimeout.plus(waitTimeout);
    }
}

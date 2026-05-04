package yagubogu.crawling.game.service.crawler;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import yagubogu.crawling.game.config.KboCrawlerProperties;

@Slf4j
public class KboHtmlClient {

    private static final DateTimeFormatter GAME_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String SCOREBOARD_SCRIPT_MANAGER =
            "ctl00$ctl00$ctl00$cphContents$cphContents$cphContents$scriptmanager1";
    private static final String SCOREBOARD_UPDATE_PANEL =
            "ctl00$ctl00$ctl00$cphContents$cphContents$cphContents$udpRecord";
    private static final String SCOREBOARD_UPDATE_PANEL_CLIENT_ID =
            "cphContents_cphContents_cphContents_udpRecord";
    private static final String SCOREBOARD_CALENDAR_SELECT_BUTTON =
            "ctl00$ctl00$ctl00$cphContents$cphContents$cphContents$btnCalendarSelect";
    private static final String SCOREBOARD_SEARCH_DATE =
            "ctl00$ctl00$ctl00$cphContents$cphContents$cphContents$hfSearchDate";
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
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
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

    public Document fetchScoreboard(final LocalDate date) {
        String url = properties.getCrawler().getScoreBoardUrl();
        Document initialDocument = getDocument(url);
        String updatePanelFragment = postScoreboard(date, url, initialDocument);
        return Jsoup.parseBodyFragment(updatePanelFragment, url);
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

    private String postScoreboard(final LocalDate date, final String url, final Document initialDocument) {
        Map<String, String> form = hiddenFields(initialDocument);
        form.put(SCOREBOARD_SCRIPT_MANAGER, SCOREBOARD_UPDATE_PANEL + "|" + SCOREBOARD_CALENDAR_SELECT_BUTTON);
        form.put("__EVENTTARGET", SCOREBOARD_CALENDAR_SELECT_BUTTON);
        form.put("__EVENTARGUMENT", "");
        form.put(SCOREBOARD_SEARCH_DATE, GAME_DATE_FORMATTER.format(date));
        form.put("__ASYNCPOST", "true");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(resolveRequestTimeout())
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Origin", properties.getCrawler().getBaseUrl())
                .header("Referer", url)
                .header("X-MicrosoftAjax", "Delta=true")
                .header("X-Requested-With", "XMLHttpRequest")
                .POST(HttpRequest.BodyPublishers.ofString(formBody(form), StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("KBO 스코어보드 요청 실패: status=" + response.statusCode() + ", url=" + url);
            }
            return extractScoreboardUpdatePanel(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("KBO 스코어보드 요청 실패: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("KBO 스코어보드 요청 중 인터럽트 발생: " + url, e);
        }
    }

    private Map<String, String> hiddenFields(final Document document) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Element input : document.select("input[type=hidden][name]")) {
            fields.put(input.attr("name"), input.attr("value"));
        }
        return fields;
    }

    private String formBody(final Map<String, String> form) {
        return form.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(final String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String extractScoreboardUpdatePanel(final String responseBody) {
        return MicrosoftAjaxDeltaParser.findUpdatePanel(responseBody, SCOREBOARD_UPDATE_PANEL)
                .or(() -> MicrosoftAjaxDeltaParser.findUpdatePanel(responseBody, SCOREBOARD_UPDATE_PANEL_CLIENT_ID))
                .orElse(responseBody);
    }

    private Duration resolveRequestTimeout() {
        Duration navigationTimeout = properties.getCrawler().getNavigationTimeout();
        Duration waitTimeout = properties.getCrawler().getWaitTimeout();
        return navigationTimeout.plus(waitTimeout);
    }
}

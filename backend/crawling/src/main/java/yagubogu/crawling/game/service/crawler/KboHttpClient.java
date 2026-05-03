package yagubogu.crawling.game.service.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;

@Slf4j
public class KboHttpClient {

    private static final DateTimeFormatter GAME_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final KboCrawlerProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public KboHttpClient(final KboCrawlerProperties properties, final ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getCrawler().getNavigationTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public JsonNode fetchGameList(final LocalDate date) {
        String gameDate = GAME_DATE_FORMATTER.format(date);
        Map<String, String> form = new LinkedHashMap<>();
        form.put("leId", "1");
        form.put("srId", resolveSeriesIds(gameDate));
        form.put("date", gameDate);

        return postJson("/ws/Main.asmx/GetKboGameList", form, buildGameCenterReferer(date));
    }

    public JsonNode fetchBoxScore(final ReviewGameContext context) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("leId", context.leId());
        form.put("srId", context.srId());
        form.put("seasonId", context.seasonId());
        form.put("gameId", context.gameCode());

        return postJson("/ws/Schedule.asmx/GetBoxScoreScroll", form, buildReviewReferer(context.gameCode()));
    }

    public ReviewGameContext findReviewGameContext(final String gameCode) {
        LocalDate date = LocalDate.parse(gameCode.substring(0, 8), GAME_DATE_FORMATTER);
        JsonNode games = fetchGameList(date).path("game");

        for (JsonNode game : games) {
            if (gameCode.equals(text(game, "G_ID"))) {
                return new ReviewGameContext(
                        text(game, "LE_ID"),
                        text(game, "SR_ID"),
                        text(game, "SEASON_ID"),
                        gameCode
                );
            }
        }

        throw new IllegalStateException("KBO 경기 목록에서 gameCode를 찾을 수 없습니다: " + gameCode);
    }

    private JsonNode postJson(final String path, final Map<String, String> form, final String referer) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getCrawler().getBaseUrl() + path))
                .timeout(resolveRequestTimeout())
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", referer)
                .POST(HttpRequest.BodyPublishers.ofString(encodeForm(form)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("KBO 요청 실패: status=" + response.statusCode() + ", path=" + path);
            }
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("KBO 응답 파싱 실패: " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("KBO 요청 중 인터럽트 발생: " + path, e);
        }
    }

    private Duration resolveRequestTimeout() {
        Duration navigationTimeout = properties.getCrawler().getNavigationTimeout();
        Duration waitTimeout = properties.getCrawler().getWaitTimeout();
        return navigationTimeout.plus(waitTimeout);
    }

    private String buildGameCenterReferer(final LocalDate date) {
        return properties.getCrawler().getGameCenterUrl()
                + "?gameDate=" + GAME_DATE_FORMATTER.format(date);
    }

    private String buildReviewReferer(final String gameCode) {
        return properties.getCrawler().getGameCenterUrl()
                + "?gameDate=" + gameCode.substring(0, 8)
                + "&gameId=" + gameCode
                + "&section=REVIEW";
    }

    private String resolveSeriesIds(final String gameDate) {
        if (gameDate.compareTo("20241026") >= 0) {
            return "0,1,3,4,5,6,7,8,9";
        }
        if (gameDate.substring(0, 4).compareTo("2021") >= 0) {
            return "0,1,3,4,5,6,7,9";
        }
        return "0,1,3,4,5,7,9";
    }

    private String encodeForm(final Map<String, String> form) {
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (!encoded.isEmpty()) {
                encoded.append('&');
            }
            encoded.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            encoded.append('=');
            encoded.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return encoded.toString();
    }

    private String text(final JsonNode node, final String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return "";
        }
        return value.asText();
    }

    public record ReviewGameContext(String leId, String srId, String seasonId, String gameCode) {
    }
}

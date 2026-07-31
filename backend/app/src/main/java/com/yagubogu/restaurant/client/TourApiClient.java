package com.yagubogu.restaurant.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.restaurant.config.TourApiProperties;
import com.yagubogu.restaurant.dto.RestaurantParam;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
public class TourApiClient {

    private static final String ENDPOINT = "/locationBasedList2";
    private static final String RESULT_CODE_OK = "0000";

    private final RestClient restClient;
    private final TourApiProperties props;
    private final ObjectMapper objectMapper;

    /**
     * 구장 주변 음식점(contentTypeId=39)을 한국관광공사 API로 조회합니다.
     *
     * @param stadiumId 야구장 ID (로깅용)
     * @param mapX      경도 (longitude)
     * @param mapY      위도 (latitude)
     */
    public List<RestaurantParam> fetchRestaurantsNear(long stadiumId, double mapX, double mapY) {
        // ServiceKey는 공공데이터포털에서 발급된 디코딩된 값을 환경변수에 저장해야 합니다.
        // UriComponentsBuilder가 자동으로 URL 인코딩하므로 이미 인코딩된 키를 저장하면 이중 인코딩됩니다.
        URI uri = UriComponentsBuilder
                .fromUriString(props.getBaseUrl() + ENDPOINT)
                .queryParam("serviceKey", props.getServiceKey())
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Yagubogu")
                .queryParam("_type", "json")
                .queryParam("mapX", mapX)
                .queryParam("mapY", mapY)
                .queryParam("radius", props.getRadius())
                .queryParam("contentTypeId", 39)
                .queryParam("numOfRows", props.getNumOfRows())
                .queryParam("pageNo", 1)
                .build()
                .toUri();

        try {
            String json = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            return parseItems(json, stadiumId);
        } catch (RestClientException e) {
            log.error("[TourApi] HTTP error for stadiumId={}: {}", stadiumId, e.getMessage());
            throw new TourApiException("Tour API call failed for stadiumId=" + stadiumId, e);
        }
    }

    List<RestaurantParam> parseItems(String json, long stadiumId) {
        try {
            JsonNode root = objectMapper.readTree(json);
            // 실제 KTO API 응답 구조: { "header": {...}, "body": {...} }
            // "response" 래퍼 없음
            JsonNode header = root.path("header");
            String resultCode = header.path("resultCode").asText();

            if (!RESULT_CODE_OK.equals(resultCode)) {
                log.warn("[TourApi] Non-OK response for stadiumId={}: code={}, msg={}",
                        stadiumId, resultCode, header.path("resultMsg").asText());
                return Collections.emptyList();
            }

            JsonNode itemsNode = root.path("body").path("items");
            // KTO API는 결과가 없을 때 items를 빈 문자열("")로 반환합니다.
            if (itemsNode.isMissingNode() || itemsNode.isTextual()) {
                return Collections.emptyList();
            }

            JsonNode itemNode = itemsNode.path("item");
            if (itemNode.isMissingNode()) {
                return Collections.emptyList();
            }

            List<RestaurantParam> result = new ArrayList<>();
            if (itemNode.isArray()) {
                // 다건: "item": [...]
                for (JsonNode item : itemNode) {
                    result.add(toParam(item, stadiumId));
                }
            } else if (itemNode.isObject()) {
                // 단건: "item": {...}
                result.add(toParam(itemNode, stadiumId));
            }
            return result;
        } catch (Exception e) {
            log.error("[TourApi] Failed to parse response for stadiumId={}: {}", stadiumId, e.getMessage());
            throw new TourApiException("Failed to parse Tour API response", e);
        }
    }

    private RestaurantParam toParam(JsonNode item, long stadiumId) {
        return new RestaurantParam(
                item.path("contentid").asText(),
                stadiumId,
                item.path("title").asText(),
                nullableText(item, "addr1"),
                item.path("mapx").asDouble(),
                item.path("mapy").asDouble(),
                nullableDistance(item, "dist"),
                nullableText(item, "tel"),
                nullableText(item, "firstimage")
        );
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) {
            return null;
        }
        String text = n.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private Integer nullableDistance(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) {
            return null;
        }
        String text = n.asText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return (int) Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

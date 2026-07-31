package com.yagubogu.place.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yagubogu.place.config.TourApiProperties;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceParam;
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

    private static final String LIST_ENDPOINT = "/locationBasedList2";
    private static final String DETAIL_COMMON_ENDPOINT = "/detailCommon2";
    private static final String DETAIL_INTRO_ENDPOINT = "/detailIntro2";
    private static final String RESULT_CODE_OK = "0000";

    private final RestClient restClient;
    private final TourApiProperties props;
    private final ObjectMapper objectMapper;

    /**
     * 구장 주변 장소를 카테고리(contentTypeId, 카페는 cat3 추가)별로 한국관광공사 API로 조회합니다.
     *
     * @param category  장소 카테고리
     * @param stadiumId 야구장 ID (로깅용)
     * @param mapX      경도 (longitude)
     * @param mapY      위도 (latitude)
     */
    public List<PlaceParam> fetchPlacesNear(PlaceCategory category, long stadiumId, double mapX, double mapY) {
        // ServiceKey는 공공데이터포털에서 발급된 디코딩된 값을 환경변수에 저장해야 합니다.
        // UriComponentsBuilder가 자동으로 URL 인코딩하므로 이미 인코딩된 키를 저장하면 이중 인코딩됩니다.
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(props.getBaseUrl() + LIST_ENDPOINT)
                .queryParam("serviceKey", props.getServiceKey())
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Yagubogu")
                .queryParam("_type", "json")
                .queryParam("mapX", mapX)
                .queryParam("mapY", mapY)
                .queryParam("radius", props.getRadius())
                .queryParam("contentTypeId", category.getContentTypeId())
                .queryParam("numOfRows", props.getNumOfRows())
                .queryParam("pageNo", 1);

        if (category.getCat3() != null) {
            builder.queryParam("cat3", category.getCat3());
        }

        URI uri = builder.build().toUri();

        try {
            String json = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            return parseItems(json, stadiumId);
        } catch (RestClientException e) {
            log.error("[TourApi] HTTP error for stadiumId={} category={}: {}", stadiumId, category, e.getMessage());
            throw new TourApiException("Tour API call failed for stadiumId=" + stadiumId, e);
        }
    }

    /**
     * 장소 상세 정보(detailCommon2 + detailIntro2)를 조회해 하나의 JSON 문자열로 합쳐 반환합니다.
     * 실패해도 목록 동기화 자체는 계속되어야 하므로 예외를 던지지 않고 null을 반환합니다.
     *
     * <p>detailCommon2는 contentTypeId를 파라미터로 받지 않는다(INVALID_REQUEST_PARAMETER_ERROR).
     * detailIntro2는 카테고리별 필드를 구분해 내려주기 위해 contentTypeId가 필수다.</p>
     */
    public String fetchDetail(String contentId, int contentTypeId) {
        try {
            JsonNode common = callDetailEndpoint(DETAIL_COMMON_ENDPOINT, contentId, null);
            JsonNode intro = callDetailEndpoint(DETAIL_INTRO_ENDPOINT, contentId, contentTypeId);

            ObjectNode merged = objectMapper.createObjectNode();
            merged.set("common", common);
            merged.set("intro", intro);
            return objectMapper.writeValueAsString(merged);
        } catch (Exception e) {
            log.warn("[TourApi] Failed to fetch detail for contentId={}: {}", contentId, e.getMessage());
            return null;
        }
    }

    private JsonNode callDetailEndpoint(String endpoint, String contentId, Integer contentTypeId) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(props.getBaseUrl() + endpoint)
                .queryParam("serviceKey", props.getServiceKey())
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Yagubogu")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId);

        if (contentTypeId != null) {
            builder.queryParam("contentTypeId", contentTypeId);
        }

        URI uri = builder.build().toUri();

        String json = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(json);
        JsonNode body = extractBody(root, "contentId=" + contentId);
        if (body == null) {
            return objectMapper.createObjectNode();
        }

        JsonNode itemNode = body.path("items").path("item");
        if (itemNode.isArray() && !itemNode.isEmpty()) {
            return itemNode.get(0);
        }
        return itemNode;
    }

    List<PlaceParam> parseItems(String json, long stadiumId) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode body = extractBody(root, "stadiumId=" + stadiumId);
            if (body == null) {
                return Collections.emptyList();
            }

            JsonNode itemsNode = body.path("items");
            // KTO API는 결과가 없을 때 items를 빈 문자열("")로 반환합니다.
            if (itemsNode.isMissingNode() || itemsNode.isTextual()) {
                return Collections.emptyList();
            }

            JsonNode itemNode = itemsNode.path("item");
            if (itemNode.isMissingNode()) {
                return Collections.emptyList();
            }

            List<PlaceParam> result = new ArrayList<>();
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

    /**
     * KTO API 응답 포맷:
     * - 성공: {"response": {"header": {"resultCode": "0000", ...}, "body": {...}}}
     * - 파라미터/인증 오류: {"resultCode": "10", "resultMsg": "..."} ("response"/"header" 래퍼 없이 최상위에 옴)
     *
     * @return 정상 응답의 body 노드, 오류/비정상 응답이면 null
     */
    private JsonNode extractBody(JsonNode root, String logContext) {
        JsonNode envelope = root.path("response");
        if (envelope.isMissingNode()) {
            log.warn("[TourApi] Error response for {}: code={}, msg={}",
                    logContext, root.path("resultCode").asText(), root.path("resultMsg").asText());
            return null;
        }

        JsonNode header = envelope.path("header");
        String resultCode = header.path("resultCode").asText();
        if (!RESULT_CODE_OK.equals(resultCode)) {
            log.warn("[TourApi] Non-OK response for {}: code={}, msg={}",
                    logContext, resultCode, header.path("resultMsg").asText());
            return null;
        }

        return envelope.path("body");
    }

    private PlaceParam toParam(JsonNode item, long stadiumId) {
        return new PlaceParam(
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

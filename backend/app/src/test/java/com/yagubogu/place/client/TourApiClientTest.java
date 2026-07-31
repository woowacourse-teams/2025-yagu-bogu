package com.yagubogu.place.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.place.config.TourApiProperties;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceParam;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 픽스처 JSON은 실제 KTO TourAPI4.0(KorService2)를 서비스키로 직접 호출해 확인한 실제 응답 구조를 반영한다.
 * - 성공 응답: {"response": {"header": {...}, "body": {...}}}
 * - 파라미터/인증 오류 응답: {"resultCode": "10", "resultMsg": "..."} ("response"/"header" 래퍼 없음)
 */
@ExtendWith(MockitoExtension.class)
class TourApiClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private TourApiClient tourApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TourApiProperties props = new TourApiProperties(
                "test-service-key",
                "https://apis.data.go.kr/B551011/KorService2",
                1000, 50,
                Duration.ofSeconds(10), Duration.ofSeconds(30)
        );
        tourApiClient = new TourApiClient(restClient, props, objectMapper);
    }

    @DisplayName("유효한 API 응답을 PlaceParam 리스트로 파싱한다")
    @Test
    void parseItems_유효한_응답_파싱() {
        String json = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": {
                      "numOfRows": 50,
                      "pageNo": 1,
                      "totalCount": 1,
                      "items": {
                        "item": [
                          {
                            "contentid": "2779091",
                            "contenttypeid": "39",
                            "title": "잠실 원조 순대국밥",
                            "addr1": "서울특별시 송파구 올림픽로 25",
                            "addr2": "",
                            "mapx": "127.0715",
                            "mapy": "37.5122",
                            "dist": "320.5",
                            "tel": "02-123-4567",
                            "firstimage": "https://example.com/image.jpg"
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        List<PlaceParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).hasSize(1);
        PlaceParam param = result.get(0);
        assertThat(param.contentId()).isEqualTo("2779091");
        assertThat(param.stadiumId()).isEqualTo(1L);
        assertThat(param.title()).isEqualTo("잠실 원조 순대국밥");
        assertThat(param.address()).isEqualTo("서울특별시 송파구 올림픽로 25");
        assertThat(param.mapX()).isEqualTo(127.0715);
        assertThat(param.mapY()).isEqualTo(37.5122);
        assertThat(param.distance()).isEqualTo(320);
        assertThat(param.tel()).isEqualTo("02-123-4567");
        assertThat(param.imageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @DisplayName("items가 빈 문자열일 때 빈 리스트를 반환한다 (KTO API 결과 없음 엣지케이스)")
    @Test
    void parseItems_빈_문자열_items_빈_리스트_반환() {
        String json = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": {
                      "numOfRows": 50,
                      "pageNo": 1,
                      "totalCount": 0,
                      "items": ""
                    }
                  }
                }
                """;

        List<PlaceParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).isEmpty();
    }

    @DisplayName("resultCode가 0000이 아니면 예외를 던진다 (빈 리스트로 처리하면 정상 데이터가 삭제될 수 있음)")
    @Test
    void parseItems_비정상_resultCode_예외_발생() {
        String json = """
                {
                  "response": {
                    "header": { "resultCode": "99", "resultMsg": "SERVICE_ERROR" },
                    "body": { "items": "" }
                  }
                }
                """;

        assertThatThrownBy(() -> tourApiClient.parseItems(json, 1L))
                .isInstanceOf(TourApiException.class)
                .hasMessageContaining("code=99");
    }

    @DisplayName("파라미터/인증 오류처럼 response 래퍼 없이 resultCode가 최상위에 오는 응답도 예외를 던진다")
    @Test
    void parseItems_response_래퍼_없는_오류_응답_예외_발생() {
        String json = """
                {
                  "responseTime": "2026-08-01T02:17:32.250",
                  "resultCode": "10",
                  "resultMsg": "INVALID_REQUEST_PARAMETER_ERROR(contentTypeId)"
                }
                """;

        assertThatThrownBy(() -> tourApiClient.parseItems(json, 1L))
                .isInstanceOf(TourApiException.class)
                .hasMessageContaining("code=10");
    }

    @DisplayName("선택 필드(tel, firstimage, addr1)가 없거나 비어있으면 null로 처리한다")
    @Test
    void parseItems_선택_필드_누락_시_null_처리() {
        String json = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": {
                      "numOfRows": 50,
                      "pageNo": 1,
                      "totalCount": 1,
                      "items": {
                        "item": [
                          {
                            "contentid": "9999",
                            "title": "필드 없는 장소",
                            "mapx": "127.0",
                            "mapy": "37.5",
                            "dist": ""
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        List<PlaceParam> result = tourApiClient.parseItems(json, 2L);

        assertThat(result).hasSize(1);
        PlaceParam param = result.get(0);
        assertThat(param.address()).isNull();
        assertThat(param.tel()).isNull();
        assertThat(param.imageUrl()).isNull();
        assertThat(param.distance()).isNull();
    }

    @DisplayName("여러 아이템을 한 번에 파싱한다 (item이 배열)")
    @Test
    void parseItems_여러_아이템_파싱() {
        String json = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": {
                      "numOfRows": 50,
                      "pageNo": 1,
                      "totalCount": 3,
                      "items": {
                        "item": [
                          { "contentid": "1", "title": "장소A", "mapx": "127.0", "mapy": "37.5" },
                          { "contentid": "2", "title": "장소B", "mapx": "127.1", "mapy": "37.6" },
                          { "contentid": "3", "title": "장소C", "mapx": "127.2", "mapy": "37.7" }
                        ]
                      }
                    }
                  }
                }
                """;

        List<PlaceParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(PlaceParam::contentId)
                .containsExactly("1", "2", "3");
    }

    @DisplayName("단건 결과일 때 item이 배열이 아닌 단일 객체로 와도 파싱한다")
    @Test
    void parseItems_단건_item_객체_파싱() {
        String json = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": {
                      "numOfRows": 50,
                      "pageNo": 1,
                      "totalCount": 1,
                      "items": {
                        "item": {
                          "contentid": "9001",
                          "title": "단건 장소",
                          "addr1": "서울 강남구",
                          "mapx": "127.0495",
                          "mapy": "37.5145",
                          "dist": "450",
                          "tel": "02-999-1234",
                          "firstimage": "https://img.com/food.jpg"
                        }
                      }
                    }
                  }
                }
                """;

        List<PlaceParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).hasSize(1);
        PlaceParam param = result.get(0);
        assertThat(param.contentId()).isEqualTo("9001");
        assertThat(param.title()).isEqualTo("단건 장소");
        assertThat(param.distance()).isEqualTo(450);
    }

    @DisplayName("카테고리별로 실제 요청 URI의 contentTypeId/cat3 파라미터가 올바르게 구성된다 (카페는 39+cat3, 나머지는 cat3 없음)")
    @Test
    void fetchPlacesNear_카테고리별_요청_파라미터_검증() {
        RestClient.RequestHeadersUriSpec<?> uriSpec = restClient.get();
        when(uriSpec.uri(any(URI.class)).retrieve().body(String.class))
                .thenReturn("""
                        { "response": { "header": { "resultCode": "0000", "resultMsg": "OK" }, "body": { "items": "" } } }
                        """);
        // 위 when(...) 스텁 설정 자체가 deep stub 체인을 실제로 호출하므로, 검증 전에 그 호출 기록을 지운다.
        clearInvocations(uriSpec);

        for (PlaceCategory category : PlaceCategory.values()) {
            tourApiClient.fetchPlacesNear(category, 1L, 127.0, 37.5);
        }

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        verify(uriSpec, times(PlaceCategory.values().length)).uri(captor.capture());

        List<URI> requestedUris = captor.getAllValues();
        PlaceCategory[] categories = PlaceCategory.values();

        for (int i = 0; i < categories.length; i++) {
            PlaceCategory category = categories[i];
            MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(requestedUris.get(i))
                    .build()
                    .getQueryParams();

            assertThat(params.getFirst("contentTypeId"))
                    .as("%s의 contentTypeId", category)
                    .isEqualTo(String.valueOf(category.getContentTypeId()));

            if (category.getCat3() != null) {
                assertThat(params.getFirst("cat3"))
                        .as("%s의 cat3 (음식점과 구분되는 카페 전용 서비스분류코드)", category)
                        .isEqualTo(category.getCat3());
            } else {
                assertThat(params.containsKey("cat3"))
                        .as("%s는 cat3 파라미터가 없어야 함", category)
                        .isFalse();
            }
        }

        // 음식점과 카페는 같은 contentTypeId(39)를 쓰지만 cat3로 구분됨을 명시적으로 확인
        // (2026-08-01 실제 서비스키로 호출 검증: cat3=A05020900 필터링 시 "카페엠", "젠제로" 등 카페만 반환됨)
        int restaurantIndex = indexOf(categories, PlaceCategory.RESTAURANT);
        int cafeIndex = indexOf(categories, PlaceCategory.CAFE);
        MultiValueMap<String, String> restaurantParams = UriComponentsBuilder
                .fromUri(requestedUris.get(restaurantIndex)).build().getQueryParams();
        MultiValueMap<String, String> cafeParams = UriComponentsBuilder
                .fromUri(requestedUris.get(cafeIndex)).build().getQueryParams();

        assertThat(restaurantParams.getFirst("contentTypeId")).isEqualTo(cafeParams.getFirst("contentTypeId"));
        assertThat(restaurantParams.containsKey("cat3")).isFalse();
        assertThat(cafeParams.getFirst("cat3")).isEqualTo("A05020900");
    }

    private static int indexOf(PlaceCategory[] categories, PlaceCategory target) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == target) {
                return i;
            }
        }
        throw new IllegalArgumentException("Not found: " + target);
    }

    @DisplayName("HTTP 호출 실패 시 TourApiException을 던진다")
    @Test
    void fetchPlacesNear_HTTP_실패_시_TourApiException() {
        when(restClient.get().uri(any(URI.class)).retrieve().body(String.class))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> tourApiClient.fetchPlacesNear(PlaceCategory.RESTAURANT, 1L, 127.07, 37.51))
                .isInstanceOf(TourApiException.class)
                .hasMessageContaining("Tour API call failed for stadiumId=1");
    }

    @DisplayName("detailCommon2와 detailIntro2 응답을 하나의 JSON으로 합쳐 반환한다")
    @Test
    void fetchDetail_공통_소개_정보_병합() throws Exception {
        String commonJson = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": { "items": { "item": { "overview": "맛집 소개", "homepage": "" } } }
                  }
                }
                """;
        String introJson = """
                {
                  "response": {
                    "header": { "resultCode": "0000", "resultMsg": "OK" },
                    "body": { "items": { "item": { "opentimefood": "11:00~22:00" } } }
                  }
                }
                """;

        when(restClient.get().uri(any(URI.class)).retrieve().body(String.class))
                .thenReturn(commonJson)
                .thenReturn(introJson);

        String detail = tourApiClient.fetchDetail("2779091", 39);

        assertThat(detail).isNotNull();
        var node = objectMapper.readTree(detail);
        assertThat(node.path("common").path("overview").asText()).isEqualTo("맛집 소개");
        assertThat(node.path("intro").path("opentimefood").asText()).isEqualTo("11:00~22:00");
    }

    @DisplayName("detailCommon2 요청에는 contentTypeId가 없고, detailIntro2 요청에는 있다 "
            + "(detailCommon2는 contentTypeId를 받으면 INVALID_REQUEST_PARAMETER_ERROR)")
    @Test
    void fetchDetail_detailCommon2는_contentTypeId_없이_detailIntro2는_포함해서_호출() {
        RestClient.RequestHeadersUriSpec<?> uriSpec = restClient.get();
        when(uriSpec.uri(any(URI.class)).retrieve().body(String.class))
                .thenReturn("""
                        { "response": { "header": { "resultCode": "0000", "resultMsg": "OK" }, "body": { "items": "" } } }
                        """);
        clearInvocations(uriSpec);

        tourApiClient.fetchDetail("2869452", 39);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        verify(uriSpec, times(2)).uri(captor.capture());

        URI commonUri = captor.getAllValues().get(0);
        URI introUri = captor.getAllValues().get(1);

        assertThat(commonUri.getPath()).endsWith("/detailCommon2");
        assertThat(introUri.getPath()).endsWith("/detailIntro2");

        MultiValueMap<String, String> commonParams =
                UriComponentsBuilder.fromUri(commonUri).build().getQueryParams();
        MultiValueMap<String, String> introParams =
                UriComponentsBuilder.fromUri(introUri).build().getQueryParams();

        assertThat(commonParams.containsKey("contentTypeId"))
                .as("detailCommon2는 contentTypeId 파라미터를 받으면 안 됨")
                .isFalse();
        assertThat(introParams.getFirst("contentTypeId")).isEqualTo("39");
    }

    @DisplayName("상세 정보 조회가 실패해도 예외를 던지지 않고 null을 반환한다")
    @Test
    void fetchDetail_실패_시_null_반환() {
        when(restClient.get().uri(any(URI.class)).retrieve().body(String.class))
                .thenThrow(new RestClientException("timeout"));

        String detail = tourApiClient.fetchDetail("2779091", 39);

        assertThat(detail).isNull();
    }
}

package com.yagubogu.restaurant.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.restaurant.config.TourApiProperties;
import com.yagubogu.restaurant.dto.RestaurantParam;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

    @DisplayName("유효한 API 응답을 RestaurantParam 리스트로 파싱한다")
    @Test
    void parseItems_유효한_응답_파싱() {
        String json = """
                {
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
                """;

        List<RestaurantParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).hasSize(1);
        RestaurantParam param = result.get(0);
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
                  "header": { "resultCode": "0000", "resultMsg": "OK" },
                  "body": {
                    "numOfRows": 50,
                    "pageNo": 1,
                    "totalCount": 0,
                    "items": ""
                  }
                }
                """;

        List<RestaurantParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).isEmpty();
    }

    @DisplayName("resultCode가 0000이 아니면 빈 리스트를 반환한다")
    @Test
    void parseItems_비정상_resultCode_빈_리스트_반환() {
        String json = """
                {
                  "header": { "resultCode": "99", "resultMsg": "SERVICE_ERROR" },
                  "body": { "items": "" }
                }
                """;

        List<RestaurantParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).isEmpty();
    }

    @DisplayName("선택 필드(tel, firstimage, addr1)가 없거나 비어있으면 null로 처리한다")
    @Test
    void parseItems_선택_필드_누락_시_null_처리() {
        String json = """
                {
                  "header": { "resultCode": "0000", "resultMsg": "OK" },
                  "body": {
                    "numOfRows": 50,
                    "pageNo": 1,
                    "totalCount": 1,
                    "items": {
                      "item": [
                        {
                          "contentid": "9999",
                          "title": "필드 없는 맛집",
                          "mapx": "127.0",
                          "mapy": "37.5",
                          "dist": ""
                        }
                      ]
                    }
                  }
                }
                """;

        List<RestaurantParam> result = tourApiClient.parseItems(json, 2L);

        assertThat(result).hasSize(1);
        RestaurantParam param = result.get(0);
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
                  "header": { "resultCode": "0000", "resultMsg": "OK" },
                  "body": {
                    "numOfRows": 50,
                    "pageNo": 1,
                    "totalCount": 3,
                    "items": {
                      "item": [
                        { "contentid": "1", "title": "맛집A", "mapx": "127.0", "mapy": "37.5" },
                        { "contentid": "2", "title": "맛집B", "mapx": "127.1", "mapy": "37.6" },
                        { "contentid": "3", "title": "맛집C", "mapx": "127.2", "mapy": "37.7" }
                      ]
                    }
                  }
                }
                """;

        List<RestaurantParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(RestaurantParam::contentId)
                .containsExactly("1", "2", "3");
    }

    @DisplayName("단건 결과일 때 item이 배열이 아닌 단일 객체로 와도 파싱한다")
    @Test
    void parseItems_단건_item_객체_파싱() {
        String json = """
                {
                  "header": { "resultCode": "0000", "resultMsg": "OK" },
                  "body": {
                    "numOfRows": 50,
                    "pageNo": 1,
                    "totalCount": 1,
                    "items": {
                      "item": {
                        "contentid": "9001",
                        "title": "단건 맛집",
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
                """;

        List<RestaurantParam> result = tourApiClient.parseItems(json, 1L);

        assertThat(result).hasSize(1);
        RestaurantParam param = result.get(0);
        assertThat(param.contentId()).isEqualTo("9001");
        assertThat(param.title()).isEqualTo("단건 맛집");
        assertThat(param.distance()).isEqualTo(450);
    }

    @DisplayName("HTTP 호출 실패 시 TourApiException을 던진다")
    @Test
    void fetchRestaurantsNear_HTTP_실패_시_TourApiException() {
        when(restClient.get().uri(any(URI.class)).retrieve().body(String.class))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> tourApiClient.fetchRestaurantsNear(1L, 127.07, 37.51))
                .isInstanceOf(TourApiException.class)
                .hasMessageContaining("Tour API call failed for stadiumId=1");
    }
}

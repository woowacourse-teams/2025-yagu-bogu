package com.yagubogu.reward.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.reward.config.KakaoGiftProperties;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

class KakaoGiftOrderStatusClientTest {

    @DisplayName("외부 주문 번호와 인증 정보로 주문 상태를 조회한다")
    @Test
    void findByExternalOrderId() {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "KakaoAK api-key"))
                .andRespond(withSuccess("""
                        {
                          "template_reserve_orders": [
                            {
                              "reserve_trace_id": 202607200000000001,
                              "external_order_id": "order-id",
                              "reserve_order_status": "PROCESSING"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GiftOrderLookupResult result = fixture.client().findByExternalOrderId("order-id");

        assertThat(result).isEqualTo(new GiftOrderLookupResult.Found(
                202607200000000001L,
                GiftOrderVendorStatus.PROCESSING
        ));
        fixture.server().verify();
    }

    @DisplayName("실패 이력과 함께 존재하는 유일한 주문을 찾는다")
    @Test
    void findExistingOrderAmongFailures() {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withSuccess("""
                        {
                          "template_reserve_orders": [
                            {
                              "external_order_id": "order-id",
                              "reserve_order_status": "ORDER_CREATE_FAILED"
                            },
                            {
                              "reserve_trace_id": 100,
                              "external_order_id": "order-id",
                              "reserve_order_status": "ORDER_CREATED"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GiftOrderLookupResult result = fixture.client().findByExternalOrderId("order-id");

        assertThat(result).isEqualTo(new GiftOrderLookupResult.Found(
                100L,
                GiftOrderVendorStatus.ORDER_CREATED
        ));
    }

    @DisplayName("조회 결과가 생성 실패 상태로만 구성되면 생성 실패로 분류한다")
    @Test
    void classifyCreationFailed() {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withSuccess("""
                        {
                          "template_reserve_orders": [
                            {
                              "external_order_id": "order-id",
                              "reserve_order_status": "INVALID_RECEIVER"
                            },
                            {
                              "external_order_id": "order-id",
                              "reserve_order_status": "ORDER_CREATE_FAILED"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GiftOrderLookupResult result = fixture.client().findByExternalOrderId("order-id");

        assertThat(result).isEqualTo(new GiftOrderLookupResult.CreationFailed(
                GiftOrderVendorStatus.INVALID_RECEIVER
        ));
    }

    @DisplayName("조회 대상 없음 오류의 숫자 코드를 NotFound로 분류한다")
    @Test
    void classifyNumericNotFoundError() {
        assertNotFound("""
                {
                  "error_name": "template_order_reserve_not_found",
                  "error_code": -22509
                }
                """);
    }

    @DisplayName("조회 대상 없음 오류의 문자 코드를 NotFound로 분류한다")
    @Test
    void classifyTextNotFoundError() {
        assertNotFound("""
                {
                  "error_name": "template_order_reserve_not_found",
                  "error_code": "-22509"
                }
                """);
    }

    @DisplayName("존재하는 주문이 여러 개면 결과를 확정하지 않는다")
    @Test
    void classifyMultipleExistingOrdersAsUncertain() {
        assertUncertainSuccess("""
                {
                  "template_reserve_orders": [
                    {
                      "reserve_trace_id": 100,
                      "external_order_id": "order-id",
                      "reserve_order_status": "WAIT"
                    },
                    {
                      "reserve_trace_id": 101,
                      "external_order_id": "order-id",
                      "reserve_order_status": "GIFT_CREATED"
                    }
                  ]
                }
                """);
    }

    @DisplayName("존재하는 주문의 추적 ID가 양수가 아니면 결과를 확정하지 않는다")
    @Test
    void classifyInvalidReserveTraceIdAsUncertain() {
        assertUncertainSuccess("""
                {
                  "template_reserve_orders": [
                    {
                      "reserve_trace_id": 0,
                      "external_order_id": "order-id",
                      "reserve_order_status": "ORDER_CREATED"
                    }
                  ]
                }
                """);
    }

    @DisplayName("중복 상태만 조회되면 결과를 확정하지 않는다")
    @Test
    void classifyDuplicateOnlyAsUncertain() {
        assertUncertainSuccess("""
                {
                  "template_reserve_orders": [
                    {
                      "external_order_id": "order-id",
                      "reserve_order_status": "DUPLICATE_TEMPLATE_ORDER"
                    }
                  ]
                }
                """);
    }

    @DisplayName("알 수 없는 상태가 포함되면 결과를 확정하지 않는다")
    @Test
    void classifyUnknownStatusAsUncertain() {
        assertUncertainSuccess("""
                {
                  "template_reserve_orders": [
                    {
                      "reserve_trace_id": 100,
                      "external_order_id": "order-id",
                      "reserve_order_status": "NEW_STATUS"
                    }
                  ]
                }
                """);
    }

    @DisplayName("응답 본문이나 주문 목록이 비어 있으면 결과를 확정하지 않는다")
    @Test
    void classifyEmptyResponseAsUncertain() {
        assertUncertainSuccess("{}");
    }

    @DisplayName("응답 형식이 올바르지 않으면 결과를 확정하지 않는다")
    @Test
    void classifyMalformedResponseAsUncertain() {
        assertUncertainSuccess("{not-json");
    }

    @DisplayName("외부 주문 번호가 없거나 요청값과 다르면 결과를 확정하지 않는다")
    @Test
    void classifyInvalidExternalOrderIdAsUncertain() {
        assertUncertainSuccess("""
                {
                  "template_reserve_orders": [
                    {"reserve_trace_id": 100, "reserve_order_status": "ORDER_CREATED"}
                  ]
                }
                """);
        assertUncertainSuccess("""
                {
                  "template_reserve_orders": [
                    {
                      "reserve_trace_id": 100,
                      "external_order_id": "other-order-id",
                      "reserve_order_status": "ORDER_CREATED"
                    }
                  ]
                }
                """);
    }

    @DisplayName("외부 주문 번호 입력이 없으면 외부 API를 호출하지 않는다")
    @Test
    void rejectBlankExternalOrderId() {
        ClientFixture fixture = fixture();

        assertThatThrownBy(() -> fixture.client().findByExternalOrderId(null))
                .isInstanceOf(InvalidGiftOrderLookupRequestException.class);
        assertThatThrownBy(() -> fixture.client().findByExternalOrderId(" "))
                .isInstanceOf(InvalidGiftOrderLookupRequestException.class);
        fixture.server().verify();
    }

    @DisplayName("조회 대상 없음과 다른 400 응답은 결과를 확정하지 않는다")
    @Test
    void classifyOtherBadRequestAsUncertain() {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withBadRequest()
                        .body("""
                                {
                                  "error_name": "template_order_reserve_not_found",
                                  "error_code": -1
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.client().findByExternalOrderId("order-id"))
                .isInstanceOf(GiftOrderLookupUncertainException.class);
    }

    @DisplayName("호출 제한과 서버 오류는 결과를 확정하지 않는다")
    @Test
    void classifyRemoteErrorsAsUncertain() {
        ClientFixture rateLimited = fixture();
        rateLimited.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> rateLimited.client().findByExternalOrderId("order-id"))
                .isInstanceOf(GiftOrderLookupUncertainException.class);

        ClientFixture serverError = fixture();
        serverError.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> serverError.client().findByExternalOrderId("order-id"))
                .isInstanceOf(GiftOrderLookupUncertainException.class);
    }

    @DisplayName("네트워크 오류가 발생하면 결과를 확정하지 않는다")
    @Test
    void classifyResourceAccessErrorAsUncertain() {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(request -> {
                    throw new ResourceAccessException("connection failed");
                });

        assertThatThrownBy(() -> fixture.client().findByExternalOrderId("order-id"))
                .isInstanceOf(GiftOrderLookupUncertainException.class)
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    private void assertNotFound(final String body) {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withBadRequest().body(body).contentType(MediaType.APPLICATION_JSON));

        assertThat(fixture.client().findByExternalOrderId("order-id"))
                .isEqualTo(new GiftOrderLookupResult.NotFound());
    }

    private void assertUncertainSuccess(final String body) {
        ClientFixture fixture = fixture();
        fixture.server().expect(requestTo(
                        "https://gift.example.com/v1/template/order/reserve/status?external_order_id=order-id"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.client().findByExternalOrderId("order-id"))
                .isInstanceOf(GiftOrderLookupUncertainException.class);
    }

    private ClientFixture fixture() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoGiftOrderStatusClient client = new KakaoGiftOrderStatusClient(
                builder.baseUrl("https://gift.example.com").build(),
                properties(),
                new ObjectMapper()
        );
        return new ClientFixture(client, server);
    }

    private KakaoGiftProperties properties() {
        return new KakaoGiftProperties(
                "https://gift.example.com",
                "api-key",
                "template-token",
                Duration.ofSeconds(1),
                Duration.ofSeconds(1)
        );
    }

    private record ClientFixture(
            KakaoGiftOrderStatusClient client,
            MockRestServiceServer server
    ) {
    }
}

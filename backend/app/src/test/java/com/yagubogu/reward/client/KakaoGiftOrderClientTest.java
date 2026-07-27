package com.yagubogu.reward.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.yagubogu.reward.config.KakaoGiftProperties;
import com.yagubogu.reward.domain.RecipientPhoneNumber;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

class KakaoGiftOrderClientTest {

    @DisplayName("카카오 발송 요청에 인증 정보와 주문 정보를 전달한다")
    @Test
    void requestOrder() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoGiftOrderClient client = new KakaoGiftOrderClient(
                builder.baseUrl("https://gift.example.com").build(),
                properties()
        );
        server.expect(requestTo("https://gift.example.com/v1/template/order"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "KakaoAK api-key"))
                .andExpect(jsonPath("$.template_token").value("template-token"))
                .andExpect(jsonPath("$.receiver_type").value("PHONE"))
                .andExpect(jsonPath("$.external_order_id").value("order-id"))
                .andExpect(jsonPath("$.receivers[0].receiver_id").value("01012345678"))
                .andRespond(withSuccess("{\"reserve_trace_id\":202607200000000001}", MediaType.APPLICATION_JSON));

        GiftOrderResult result = client.requestOrder(
                new GiftOrderRequest("order-id", new RecipientPhoneNumber("01012345678"))
        );

        assertThat(result.reserveTraceId()).isEqualTo(202607200000000001L);
        server.verify();
    }

    @DisplayName("카카오가 요청을 거절하면 명확한 실패로 분류한다")
    @Test
    void classifyRejectedRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoGiftOrderClient client = new KakaoGiftOrderClient(
                builder.baseUrl("https://gift.example.com").build(),
                properties()
        );
        server.expect(requestTo("https://gift.example.com/v1/template/order"))
                .andRespond(withBadRequest());

        assertThatThrownBy(() -> client.requestOrder(
                new GiftOrderRequest("order-id", new RecipientPhoneNumber("01012345678"))))
                .isInstanceOf(KakaoGiftRequestRejectedException.class)
                .hasCauseInstanceOf(HttpClientErrorException.class);
        server.verify();
    }

    @DisplayName("카카오가 요청을 제한하면 결과가 불확실한 실패로 분류한다")
    @Test
    void classifyRateLimitedRequestAsUncertain() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoGiftOrderClient client = new KakaoGiftOrderClient(
                builder.baseUrl("https://gift.example.com").build(),
                properties()
        );
        server.expect(requestTo("https://gift.example.com/v1/template/order"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.requestOrder(
                new GiftOrderRequest("order-id", new RecipientPhoneNumber("01012345678"))))
                .isInstanceOf(KakaoGiftRequestUncertainException.class)
                .hasCauseInstanceOf(HttpClientErrorException.class);
        server.verify();
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
}

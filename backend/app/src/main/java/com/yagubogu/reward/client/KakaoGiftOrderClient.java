package com.yagubogu.reward.client;

import com.yagubogu.reward.config.KakaoGiftProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class KakaoGiftOrderClient implements GiftOrderClient {

    private final RestClient restClient;
    private final KakaoGiftProperties properties;

    public KakaoGiftOrderClient(
            @Qualifier("kakaoGiftRestClient") final RestClient restClient,
            final KakaoGiftProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public GiftOrderResult requestOrder(final GiftOrderRequest request) {
        validateConfiguration();
        try {
            KakaoGiftOrderResponse response = restClient.post()
                    .uri("/v1/template/order")
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.apiKey())
                    .body(KakaoGiftOrderRequest.from(request, properties.templateToken()))
                    .retrieve()
                    .body(KakaoGiftOrderResponse.class);
            if (response == null || response.reserveTraceId() == null) {
                throw new KakaoGiftRequestUncertainException(
                        "Kakao gift order response has no reserve_trace_id",
                        null
                );
            }
            return new GiftOrderResult(response.reserveTraceId());
        } catch (HttpClientErrorException exception) {
            HttpStatusCode statusCode = exception.getStatusCode();
            if (statusCode.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                throw new KakaoGiftRequestUncertainException(
                        "Kakao gift order was rate-limited",
                        exception
                );
            }
            throw new KakaoGiftRequestRejectedException(
                    "Kakao gift order was rejected: status=" + statusCode.value(),
                    exception
            );
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw new KakaoGiftRequestUncertainException("Kakao gift order result is uncertain", exception);
        }
    }

    private void validateConfiguration() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()
                || properties.templateToken() == null || properties.templateToken().isBlank()) {
            throw new KakaoGiftRequestRejectedException("Kakao gift configuration is missing");
        }
    }
}

package com.yagubogu.reward.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.reward.config.KakaoGiftProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 카카오 주문 조회 응답을 내부 조회 결과로 변환한다.
 */
@Component
public class KakaoGiftOrderStatusClient implements GiftOrderStatusClient {

    private static final String NOT_FOUND_ERROR_NAME = "template_order_reserve_not_found";
    private static final long NOT_FOUND_ERROR_CODE = -22509L;

    private final RestClient restClient;
    private final KakaoGiftProperties properties;
    private final ObjectMapper objectMapper;

    public KakaoGiftOrderStatusClient(
            @Qualifier("kakaoGiftRestClient") final RestClient restClient,
            final KakaoGiftProperties properties,
            final ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 주문 존재가 확인된 경우에만 접수 결과를 반환하고, 판단할 수 없는 응답은 예외로 처리한다.
     */
    @Override
    public GiftOrderLookupResult findByExternalOrderId(final String externalOrderId) {
        if (externalOrderId == null || externalOrderId.isBlank()) {
            throw new InvalidGiftOrderLookupRequestException("External order ID must not be blank");
        }
        try {
            KakaoGiftOrderStatusResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/template/order/reserve/status")
                            .queryParam("external_order_id", externalOrderId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.apiKey())
                    .retrieve()
                    .body(KakaoGiftOrderStatusResponse.class);
            return classify(response, externalOrderId);
        } catch (HttpClientErrorException exception) {
            if (isNotFound(exception)) {
                return new GiftOrderLookupResult.NotFound();
            }
            throw uncertain("Kakao gift order lookup was rejected", exception);
        } catch (RestClientException exception) {
            throw uncertain("Kakao gift order lookup failed", exception);
        }
    }

    private GiftOrderLookupResult classify(
            final KakaoGiftOrderStatusResponse response,
            final String externalOrderId
    ) {
        if (response == null || response.orders() == null || response.orders().isEmpty()) {
            throw uncertain("Kakao gift order lookup response is empty");
        }

        List<ClassifiedOrder> orders = classifyOrders(response.orders(), externalOrderId);
        List<ClassifiedOrder> existingOrders = orders.stream()
                .filter(order -> order.status().provesOrderExists())
                .toList();
        if (existingOrders.size() > 1) {
            throw uncertain("Kakao gift order lookup returned multiple existing orders");
        }
        if (existingOrders.size() == 1) {
            ClassifiedOrder order = existingOrders.getFirst();
            if (order.reserveTraceId() == null || order.reserveTraceId() <= 0) {
                throw uncertain("Kakao gift order lookup returned an invalid reserve_trace_id");
            }
            return new GiftOrderLookupResult.Found(order.reserveTraceId(), order.status());
        }

        boolean onlyCreationFailures = orders.stream()
                .allMatch(order -> order.status().provesCreationFailed());
        if (onlyCreationFailures) {
            return new GiftOrderLookupResult.CreationFailed(orders.getFirst().status());
        }
        throw uncertain("Kakao gift order lookup result is uncertain");
    }

    /**
     * 조회된 주문 번호와 상태값이 요청한 주문의 응답으로 신뢰할 수 있는지 확인한다.
     */
    private List<ClassifiedOrder> classifyOrders(
            final List<KakaoGiftOrderStatusResponse.Order> responseOrders,
            final String externalOrderId
    ) {
        List<ClassifiedOrder> orders = new ArrayList<>(responseOrders.size());
        for (KakaoGiftOrderStatusResponse.Order order : responseOrders) {
            if (order == null
                    || order.externalOrderId() == null
                    || order.externalOrderId().isBlank()
                    || !order.externalOrderId().equals(externalOrderId)
                    || order.status() == null
                    || order.status().isBlank()) {
                throw uncertain("Kakao gift order lookup returned a malformed order");
            }
            try {
                GiftOrderVendorStatus status = GiftOrderVendorStatus.valueOf(order.status());
                orders.add(new ClassifiedOrder(order.reserveTraceId(), status));
            } catch (IllegalArgumentException exception) {
                throw uncertain("Kakao gift order lookup returned an unknown status", exception);
            }
        }
        return orders;
    }

    /**
     * 카카오가 정의한 조회 대상 없음 오류만 주문 미조회 결과로 분류한다.
     */
    private boolean isNotFound(final HttpClientErrorException exception) {
        if (!exception.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            return false;
        }
        try {
            KakaoApiError error = objectMapper.readValue(
                    exception.getResponseBodyAsByteArray(),
                    KakaoApiError.class
            );
            return NOT_FOUND_ERROR_NAME.equals(error.errorName())
                    && hasNotFoundErrorCode(error.errorCode());
        } catch (IOException exceptionIgnored) {
            return false;
        }
    }

    private boolean hasNotFoundErrorCode(final JsonNode errorCode) {
        if (errorCode == null) {
            return false;
        }
        if (errorCode.isIntegralNumber()) {
            return errorCode.longValue() == NOT_FOUND_ERROR_CODE;
        }
        return errorCode.isTextual()
                && Long.toString(NOT_FOUND_ERROR_CODE).equals(errorCode.textValue().strip());
    }

    private GiftOrderLookupUncertainException uncertain(final String message) {
        return new GiftOrderLookupUncertainException(message);
    }

    private GiftOrderLookupUncertainException uncertain(final String message, final Throwable cause) {
        return new GiftOrderLookupUncertainException(message, cause);
    }

    private record ClassifiedOrder(
            Long reserveTraceId,
            GiftOrderVendorStatus status
    ) {
    }

    private record KakaoApiError(
            @JsonProperty("error_name") String errorName,
            @JsonProperty("error_code") JsonNode errorCode
    ) {
    }
}

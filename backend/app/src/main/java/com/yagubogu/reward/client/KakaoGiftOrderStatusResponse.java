package com.yagubogu.reward.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record KakaoGiftOrderStatusResponse(
        @JsonProperty("template_reserve_orders") List<Order> orders
) {

    record Order(
            @JsonProperty("reserve_trace_id") Long reserveTraceId,
            @JsonProperty("external_order_id") String externalOrderId,
            @JsonProperty("reserve_order_status") String status
    ) {
    }
}

package com.yagubogu.reward.client;

import com.fasterxml.jackson.annotation.JsonProperty;

record KakaoGiftOrderResponse(
        @JsonProperty("reserve_trace_id") Long reserveTraceId
) {
}

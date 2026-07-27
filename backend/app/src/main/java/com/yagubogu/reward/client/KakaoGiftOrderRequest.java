package com.yagubogu.reward.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record KakaoGiftOrderRequest(
        @JsonProperty("template_token") String templateToken,
        @JsonProperty("receiver_type") String receiverType,
        @JsonProperty("receivers") List<KakaoGiftReceiver> receivers,
        @JsonProperty("external_order_id") String externalOrderId
) {

    static KakaoGiftOrderRequest from(final GiftOrderRequest request, final String templateToken) {
        return new KakaoGiftOrderRequest(
                templateToken,
                "PHONE",
                List.of(new KakaoGiftReceiver(
                        request.externalOrderId(),
                        request.recipientPhoneNumber().getValue()
                )),
                request.externalOrderId()
        );
    }

    record KakaoGiftReceiver(
            @JsonProperty("external_key") String externalKey,
            @JsonProperty("receiver_id") String receiverId
    ) {
    }
}

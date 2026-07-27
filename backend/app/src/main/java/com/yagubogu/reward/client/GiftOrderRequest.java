package com.yagubogu.reward.client;

import com.yagubogu.reward.domain.RecipientPhoneNumber;

public record GiftOrderRequest(
        String externalOrderId,
        RecipientPhoneNumber recipientPhoneNumber
) {
}

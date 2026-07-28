package com.yagubogu.reward.client;

/**
 * 기프티콘 주문의 존재 여부와 생성 결과를 나타낸다.
 */
public sealed interface GiftOrderLookupResult {

    record Found(
            long reserveTraceId,
            GiftOrderVendorStatus status
    ) implements GiftOrderLookupResult {
    }

    record CreationFailed(
            GiftOrderVendorStatus status
    ) implements GiftOrderLookupResult {
    }

    record NotFound() implements GiftOrderLookupResult {
    }
}

package com.yagubogu.reward.domain;

/**
 * 외부 주문 추적 번호가 발급 기록에 저장할 수 없는 값일 때 발생한다.
 */
public class InvalidGifticonReserveTraceIdException extends RuntimeException {

    public InvalidGifticonReserveTraceIdException(final long reserveTraceId) {
        super("Gifticon reserve trace id must be positive: reserveTraceId=" + reserveTraceId);
    }
}

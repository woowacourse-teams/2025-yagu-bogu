package com.yagubogu.reward.client;

public enum GiftOrderVendorStatus {

    WAIT,
    PROCESSING,
    ORDER_CREATE_FAILED,
    ORDER_CREATE_FAILED_BUSY,
    ORDER_CREATED,
    GIFT_CREATED,
    GIFT_ENDED,
    CANCELED,
    ORDER_TEMPLATE_NOT_FOUND,
    NOT_ENOUGH_CASH_BALANCE,
    INVALID_RECEIVER,
    CHANGE_ORDER_TEMPLATE_SNAPSHOT,
    EXCEED_BUDGET,
    DUPLICATE_TEMPLATE_ORDER;

    boolean provesOrderExists() {
        return switch (this) {
            case WAIT, PROCESSING, ORDER_CREATED, GIFT_CREATED, GIFT_ENDED, CANCELED -> true;
            default -> false;
        };
    }

    boolean provesCreationFailed() {
        return switch (this) {
            case ORDER_CREATE_FAILED,
                 ORDER_CREATE_FAILED_BUSY,
                 ORDER_TEMPLATE_NOT_FOUND,
                 NOT_ENOUGH_CASH_BALANCE,
                 INVALID_RECEIVER,
                 CHANGE_ORDER_TEMPLATE_SNAPSHOT,
                 EXCEED_BUDGET -> true;
            default -> false;
        };
    }
}

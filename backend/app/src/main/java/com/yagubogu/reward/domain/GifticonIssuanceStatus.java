package com.yagubogu.reward.domain;

public enum GifticonIssuanceStatus {
    AWAITING_RECIPIENT_INFO,
    REQUEST_RETRYABLE,
    REQUEST_IN_PROGRESS,
    REQUEST_ACCEPTED,
    DELIVERED,
    FAILED,
    CANCELED,
}

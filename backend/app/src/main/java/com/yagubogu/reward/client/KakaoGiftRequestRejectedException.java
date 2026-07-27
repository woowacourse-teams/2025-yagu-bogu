package com.yagubogu.reward.client;

public class KakaoGiftRequestRejectedException extends RuntimeException {

    public KakaoGiftRequestRejectedException(final String message) {
        super(message);
    }

    public KakaoGiftRequestRejectedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

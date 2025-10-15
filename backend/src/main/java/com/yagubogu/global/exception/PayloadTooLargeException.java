package com.yagubogu.global.exception;

public class PayloadTooLargeException extends RuntimeException {

    public PayloadTooLargeException(final String message) {
        super(message);
    }
}

package com.yagubogu.global.exception;

public class KboClientException extends YaguBoguException {

    public KboClientException(final String message) {
        super(message);
    }

    public KboClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

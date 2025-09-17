package com.yagubogu.global.exception;

public class YaguBoguException extends RuntimeException {

    public YaguBoguException(final String message) {
        super(message);
    }

    public YaguBoguException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

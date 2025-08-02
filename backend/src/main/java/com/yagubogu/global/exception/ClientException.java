package com.yagubogu.global.exception;

public class ClientException extends YaguBoguException {

    public ClientException(final String message) {
        super(message);
    }

    public ClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

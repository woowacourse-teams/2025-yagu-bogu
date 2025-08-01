package com.yagubogu.auth.exception;

import com.yagubogu.global.exception.UnAuthorizedException;

public class InvalidTokenException extends UnAuthorizedException {

    public InvalidTokenException(final String message) {
        super(message);
    }
}

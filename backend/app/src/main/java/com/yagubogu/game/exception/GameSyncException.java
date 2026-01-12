package com.yagubogu.game.exception;

import com.yagubogu.global.exception.YaguBoguException;

public class GameSyncException extends YaguBoguException {

    public GameSyncException(final String message) {
        super(message);
    }

    public GameSyncException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

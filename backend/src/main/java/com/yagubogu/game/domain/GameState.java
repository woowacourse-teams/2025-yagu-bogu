package com.yagubogu.game.domain;

import com.yagubogu.global.exception.ClientException;
import java.util.Arrays;

public enum GameState {

    SCHEDULED(1),
    LIVE(2),
    COMPLETED(3),
    CANCELED(4),
    ;

    private final Integer stateNumber;

    GameState(final Integer stateNumber) {
        this.stateNumber = stateNumber;
    }

    public static GameState from(final Integer gameState) {
        return Arrays.stream(values())
                .filter(status -> status.stateNumber.equals(gameState))
                .findFirst()
                .orElseThrow(() -> new ClientException("Unknown game status: " + gameState));
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isNotCompleted() {
        return !isCompleted();
    }
}

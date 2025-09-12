package com.yagubogu.game.domain;

import com.yagubogu.game.exception.GameSyncException;
import java.util.Arrays;
import java.util.EnumSet;

public enum GameState {

    SCHEDULED(1),
    LIVE(2),
    COMPLETED(3),
    CANCELED(4),
    ;

    public static final EnumSet<GameState> FINALIZED_GAME_STATES = EnumSet.of(GameState.COMPLETED, GameState.CANCELED);

    private final Integer stateNumber;

    GameState(final Integer stateNumber) {
        this.stateNumber = stateNumber;
    }

    public static GameState from(final Integer gameState) {
        return Arrays.stream(values())
                .filter(status -> status.stateNumber.equals(gameState))
                .findFirst()
                .orElseThrow(() -> new GameSyncException("Unknown game status: " + gameState));
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }
}
